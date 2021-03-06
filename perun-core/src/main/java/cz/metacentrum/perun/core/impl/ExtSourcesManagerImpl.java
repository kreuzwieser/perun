package cz.metacentrum.perun.core.impl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.implApi.ExtSourcesManagerImplApi;

public class ExtSourcesManagerImpl implements ExtSourcesManagerImplApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourcesManagerImpl.class);
	public final static String USEREXTSOURCEMAPPING = "additionalues_";

	protected final static String extSourceMappingSelectQuery = "ext_sources.id as ext_sources_id, ext_sources.name as ext_sources_name, ext_sources.type as ext_sources_type, " +
		"ext_sources.created_at as ext_sources_created_at, ext_sources.created_by as ext_sources_created_by, ext_sources.modified_by as ext_sources_modified_by, " +
		"ext_sources.modified_at as ext_sources_modified_at, ext_sources.modified_by_uid as ext_sources_modified_by_uid, ext_sources.created_by_uid as ext_sources_created_by_uid";

	protected final static String extSourceMappingSelectQueryWithAttributes = "ext_sources.id as ext_sources_id, ext_sources.name as ext_sources_name, ext_sources.type as ext_sources_type, " +
			"ext_sources.created_at as ext_sources_created_at, ext_sources.created_by as ext_sources_created_by, ext_sources.modified_by as ext_sources_modified_by, " +
			"ext_sources.modified_at as ext_sources_modified_at, ext_sources.modified_by_uid as ext_sources_modified_by_uid, ext_sources.created_by_uid as ext_sources_created_by_uid, " +
			"ext_sources_attributes.attr_name as attr_name, ext_sources_attributes.attr_value as attr_value";

	// http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
	private static JdbcTemplate jdbc;

	private static final RowMapper<ExtSource> EXTSOURCE_MAPPER = new RowMapper<ExtSource>() {
		public ExtSource mapRow(ResultSet rs, int i) throws SQLException {
			try {
				Class<?> extSourceClass = Class.forName((String) rs.getString("ext_sources_type"));
				ExtSource es = (ExtSource) extSourceClass.newInstance();

				es.setId(rs.getInt("ext_sources_id"));
				es.setName(rs.getString("ext_sources_name"));
				es.setType(rs.getString("ext_sources_type"));
				es.setCreatedAt(rs.getString("ext_sources_created_at"));
				es.setCreatedBy(rs.getString("ext_sources_created_by"));
				es.setModifiedAt(rs.getString("ext_sources_modified_at"));
				es.setModifiedBy(rs.getString("ext_sources_modified_by"));
				if(rs.getInt("ext_sources_modified_by_uid") == 0) es.setModifiedByUid(null);
				else es.setModifiedByUid(rs.getInt("ext_sources_modified_by_uid"));
				if(rs.getInt("ext_sources_created_by_uid") == 0) es.setCreatedByUid(null);
				else es.setCreatedByUid(rs.getInt("ext_sources_created_by_uid"));
				return es;
			} catch (ClassNotFoundException e) {
				throw new InternalErrorRuntimeException(e);
			} catch (InstantiationException e) {
				throw new InternalErrorRuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new InternalErrorRuntimeException(e);
			} catch (RuntimeException e) {
				throw new InternalErrorRuntimeException(e);
			}
		}
	};

	private static final RowMapper<Map<String, Object>> EXT_SOURCE_ATTRIBUTES_MAPPER = new RowMapper<Map<String, Object>>() {
		public Map<String, Object> mapRow(ResultSet rs, int i) throws SQLException {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("name", rs.getString("attr_name"));
			attributes.put("value", rs.getString("attr_value"));
			return attributes;
		}
	};

	public ExtSourcesManagerImpl(DataSource perunPool) throws InternalErrorException {
		jdbc = new JdbcTemplate(perunPool);
	}

	public ExtSource createExtSource(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceExistsException {
		Utils.notNull(extSource.getName(), "extSource.getName()");
		Utils.notNull(extSource.getType(), "extSource.getType()");

		try {
			// Check if the extSources already exists
			if (0 < jdbc.queryForInt("select count(id) from ext_sources where name=? and type=?", extSource.getName(), extSource.getType())) {
				throw new ExtSourceExistsException(extSource);
			}

			// Get a new Id
			int newId = Utils.getNewId(jdbc, "ext_sources_id_seq");

			jdbc.update("insert into ext_sources (id, name, type, created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
					"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", newId, extSource.getName(),
					extSource.getType(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
					sess.getPerunPrincipal().getUserId());
			extSource.setId(newId);

			ExtSource es;

			// Get the instance by the type of the extSource
			try {
				Class<?> extSourceClass = Class.forName((String) extSource.getType());
				es = (ExtSource) extSourceClass.newInstance();
			} catch (ClassNotFoundException e) {
				throw new InternalErrorException(e);
			} catch (InstantiationException e) {
				throw new InternalErrorRuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new InternalErrorRuntimeException(e);
			}

			// Set the properties
			es.setId(extSource.getId());
			es.setName(extSource.getName());
			es.setType(extSource.getType());
			es.setAttributes(extSource.getAttributes());

			// Now store the attributes
			Map<String, String> attributes = extSource.getAttributes();
			if (attributes != null) {
				Iterator<String> i = attributes.keySet().iterator();
				while (i.hasNext()) {
					String attr_name = i.next();
					jdbc.update("insert into ext_sources_attributes (attr_name, attr_value, ext_sources_id,created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
							"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", attr_name, attributes.get(attr_name), extSource.getId(),
							sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
				}
			}

			// Assign newly created extSource
			extSource = es;

			return extSource;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void deleteExtSource(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceAlreadyRemovedException {
		try {
			// Delete associated attributes
			jdbc.update("delete from ext_sources_attributes where ext_sources_id=?", extSource.getId());
			// Delete the external source
			int numAffected = jdbc.update("delete from ext_sources where id=?", extSource.getId());
			if(numAffected == 0) throw new ExtSourceAlreadyRemovedException("ExtSource: " + extSource);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Updates extSource definition. It is only private method, because extSources are defined in the external XML file.
	 *
	 * @param sess
	 * @param extSource
	 * @throws InternalErrorException
	 */
	private void updateExtSource(PerunSession sess, ExtSource extSource) throws InternalErrorException {
		ExtSource extSourceDb;
		try {
			extSourceDb = this.getExtSourceById(sess, extSource.getId());
		} catch (ExtSourceNotExistsException e) {
			throw new InternalErrorException(e);
		}

		// Check the name
		if (!extSourceDb.getName().equals(extSource.getName())) {
			try {
				jdbc.update("update ext_sources set name=? ,modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?", extSource.getName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), extSource.getId());
			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}

		// Check the type
		if (!extSourceDb.getType().equals(extSource.getType())) {
			try {
				jdbc.update("update ext_sources set type=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?", extSource.getType(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), extSource.getId());
			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}

		// Check the attributes
		if (!extSourceDb.getAttributes().equals(extSource.getAttributes())) {
			log.debug("There is a change in the extSource attributes");
			try {
				// Firstly delete all attributes, then store new ones
				jdbc.update("delete from ext_sources_attributes where ext_sources_id=?", extSource.getId());

				for (String attrName: extSource.getAttributes().keySet()) {
					jdbc.update("insert into ext_sources_attributes (ext_sources_id, attr_name, attr_value, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
							"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", extSource.getId(), attrName,
							extSource.getAttributes().get(attrName), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
							sess.getPerunPrincipal().getUserId());
				}
			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	protected static final ExtSourcesExtractor EXT_SOURCES_EXTRACTOR = new ExtSourcesExtractor();

	private static class ExtSourcesExtractor implements ResultSetExtractor<List<ExtSource>> {

		public List<ExtSource> extractData(ResultSet rs) throws SQLException, DataAccessException {
			Map<Integer, ExtSource> map = new HashMap<>();
			ExtSource myObject;
			while (rs.next()) {
				// fetch from map by ID
				Integer id = rs.getInt("ext_sources_id");
				myObject = map.get(id);
				if(myObject == null){
					// if not preset, put in map
					myObject = EXTSOURCE_MAPPER.mapRow(rs, rs.getRow());
					map.put(id, myObject);
				}
				// fetch each attribute for extSource and add it to extSource
				Map<String, Object> attributes = EXT_SOURCE_ATTRIBUTES_MAPPER.mapRow(rs, rs.getRow());

				if (attributes.get("name") != null) {
					// add only if exists
					myObject.setAttribute((String) attributes.get("name"), (String) attributes.get("value"));
				}
			}
			return new ArrayList<ExtSource>(map.values());
		}
	}

	public ExtSource getExtSourceById(PerunSession sess, int id) throws InternalErrorException, ExtSourceNotExistsException {
		try {
			List<ExtSource> extSources = jdbc.query("select " + extSourceMappingSelectQueryWithAttributes + " from ext_sources left join ext_sources_attributes on ext_sources.id=ext_sources_attributes.ext_sources_id where id=?", EXT_SOURCES_EXTRACTOR, id);

			if (extSources != null && extSources.size()>1) {
				throw new ConsistencyErrorException("There are more than one extSources under ID="+id);
			} else if (extSources != null && extSources.isEmpty()) {
				throw new ExtSourceNotExistsException("ExtSource with ID="+id+" not exists");
			}

			if (extSources != null && extSources.size() == 1) {
				if (extSources.get(0) != null) {
					// return correct data
					return extSources.get(0);
				} else {
					throw new InternalErrorException("extSource with ID="+id+" is null.");
				}
			}
			// not correct data
			throw new InternalErrorException("Response from SQL RowExtractor is null.");
		} catch (EmptyResultDataAccessException e) {
			throw new ExtSourceNotExistsException("ExtSourceId " + id, e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public ExtSource getExtSourceByName(PerunSession sess, String name) throws InternalErrorException, ExtSourceNotExistsException {
		try {
			List<ExtSource> extSources = jdbc.query("select " + extSourceMappingSelectQueryWithAttributes +
					" from ext_sources left join ext_sources_attributes on ext_sources.id=ext_sources_attributes.ext_sources_id " +
					"where name=?", EXT_SOURCES_EXTRACTOR, name);

			if (extSources != null && extSources.size()>1) {
				throw new ConsistencyErrorException("There are more than one extSources with name="+name);
			} else if (extSources != null && extSources.isEmpty()) {
				throw new ExtSourceNotExistsException("ExtSource with name="+name+" not exists");
			}

			if (extSources != null && extSources.size() == 1) {
				if (extSources.get(0) != null) {
					// return correct data
					return extSources.get(0);
				} else {
					throw new InternalErrorException("extSource with name="+name+" is null.");
				}
			}
			// not correct data
			throw new InternalErrorException("Response from SQL RowExtractor is null.");
		} catch (EmptyResultDataAccessException e) {
			throw new ExtSourceNotExistsException("ExtSourceName " + name, e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<ExtSource> getVoExtSources(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			List<ExtSource> extSources = jdbc.query("select " + extSourceMappingSelectQueryWithAttributes +
					" from vo_ext_sources v inner join ext_sources on v.ext_sources_id=ext_sources.id " +
					"   left join ext_sources_attributes on ext_sources.id=ext_sources_attributes.ext_sources_id " +
					" where v.vo_id=?", EXT_SOURCES_EXTRACTOR, vo.getId());

			if (extSources != null) {
				return extSources;
			}
			throw new InternalErrorException("Response from SQL RowExtractor is null.");
		} catch (EmptyResultDataAccessException e) {
			// empty list
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<ExtSource> getExtSources(PerunSession sess) throws InternalErrorException {
		try {
			List<ExtSource> extSources = jdbc.query("select " + extSourceMappingSelectQueryWithAttributes +
					" from ext_sources left join ext_sources_attributes on ext_sources.id=ext_sources_attributes.ext_sources_id ", EXT_SOURCES_EXTRACTOR);

			if (extSources != null) {
				return extSources;
			}
			throw new InternalErrorException("Response from SQL RowExtractor is null.");
		} catch (EmptyResultDataAccessException e) {
			// empty list
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addExtSource(PerunSession sess, Vo vo, ExtSource source) throws InternalErrorException, ExtSourceAlreadyAssignedException {
		try {
			if(0 < jdbc.queryForInt("select count('x') from vo_ext_sources where ext_sources_id=? and vo_id=?", source.getId(), vo.getId())) {
				throw new ExtSourceAlreadyAssignedException(source);
			}

			jdbc.update("insert into vo_ext_sources (ext_sources_id, vo_id, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
					"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", source.getId(), vo.getId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeExtSource(PerunSession sess, Vo vo, ExtSource source) throws InternalErrorException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		try {
			if (jdbc.queryForInt("select count('x') from vo_ext_sources where ext_sources_id=? and vo_id=?", source.getId(), vo.getId()) == 0) {
				// Source isn't assigned
				throw new ExtSourceNotAssignedException("ExtSource id='" + source.getId() + "'");
			}

			int numAffected = jdbc.update("delete from vo_ext_sources where ext_sources_id=? and vo_id=?", source.getId(), vo.getId());
			if(numAffected != 1) throw new ExtSourceAlreadyRemovedException("ExtSource: " + source + " , Vo: " + vo);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<Integer> getAssociatedUsersIdsWithExtSource(PerunSession sess, ExtSource source) throws InternalErrorException {
		try {
			return jdbc.query("select user_id from user_ext_sources where ext_sources_id=?", Utils.ID_MAPPER, source.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Routine which initialize the extSourcesManager.
	 *
	 * @throws InternalErrorException
	 */
	public void initialize(PerunSession sess) {
		this.loadExtSourcesDefinitions(sess);

		// Check if default extSource PERUN exists
		try {
			this.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN);
		} catch (ExtSourceNotExistsException e) {
			ExtSource es = new ExtSource(ExtSourcesManager.EXTSOURCE_NAME_PERUN, ExtSourcesManager.EXTSOURCE_INTERNAL);
			try {
				this.createExtSource(sess, es);
			} catch (ExtSourceExistsException e1) {
				log.error("Trying to create default PERUN extSource which already exists.");
			} catch (InternalErrorException e1) {
				log.error("Cannot create default PERUN extSource.");
			}
		} catch (InternalErrorException e) {
			log.error("Cannot get default PERUN extSource.");
		}
	}

	/**
	 * Loads the extSources definitions from the XML configuration file.
	 * All data from the extSouces XML file are synchronized with the DB.
	 *
	 * @throws InternalErrorException
	 */
	public void loadExtSourcesDefinitions(PerunSession sess) {
		try {
			// Load the XML file
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(ExtSourcesManager.CONFIGURATIONFILE));
			if (is == null) {
				throw new InternalErrorException("Cannot load configuration file " + ExtSourcesManager.CONFIGURATIONFILE);
			}
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();

			// Check if the root element is "extSources"
			if (!doc.getDocumentElement().getNodeName().equals("extSources")) {
				throw new InternalErrorException("perun-extSources.xml doesn't contain extSources as root element");
			}

			// Get all defined extSources
			NodeList extSourcesNodes = doc.getElementsByTagName("extSource");
			for (int extSourceSeq = 0; extSourceSeq < extSourcesNodes.getLength(); extSourceSeq++) {

				// Get each extSource
				Node extSourceNode = extSourcesNodes.item(extSourceSeq);
				if (extSourceNode.getNodeType() == Node.ELEMENT_NODE) {

					Element extSourceElement = (Element) extSourceNode;

					// Get extSource name
					String extSourceName = extSourceElement.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
					if (extSourceName == null) {
						throw new InternalErrorException("extSource doesn't have defined name");
					}

					// Get extSource type
					String extSourceType = extSourceElement.getElementsByTagName("type").item(0).getChildNodes().item(0).getNodeValue();
					if (extSourceType == null) {
						throw new InternalErrorException("extSource " + extSourceName + " doesn't have defined type");
					}

					// Get all extSource attributes
					NodeList attributeNodes = extSourceElement.getElementsByTagName("attribute");

					Map<String, String> attributes = new HashMap<String, String>();
					for (int attributeSeq = 0; attributeSeq < attributeNodes.getLength(); attributeSeq++) {
						Element elem = (Element) attributeNodes.item(attributeSeq);

						if (elem.getNodeType() == Node.ELEMENT_NODE) {
							String attrName = elem.getAttribute("name");
							String attrValue = null;
							if (elem.getChildNodes() != null && elem.getChildNodes().item(0) != null) {
								attrValue = elem.getChildNodes().item(0).getNodeValue();
							}

							attributes.put(attrName, attrValue);
						}
					}

					// Check if the extSource
					try {
						ExtSource extSource;
						try {
							extSource = this.getExtSourceByName(sess, extSourceName);
							extSource.setName(extSourceName);
							extSource.setType(extSourceType);
							extSource.setAttributes(attributes);

							// ExtSource exists, so check values and potentionally update it
							this.updateExtSource(sess, extSource);

						} catch (ExtSourceNotExistsException e) {
							// ExtSource doesn't exist, so create it
							extSource = new ExtSource();
							extSource.setName(extSourceName);
							extSource.setType(extSourceType);
							extSource.setAttributes(attributes);
							extSource = this.createExtSource(sess, extSource);
						}
					} catch (RuntimeException e) {
						throw new InternalErrorException(e);
					}
				}
			}
		} catch (FileNotFoundException e) {
			log.warn("No external source configuration file found.");
		} catch (Exception e) {
			log.error("Cannot initialize ExtSourceManager.");
			throw new InternalErrorRuntimeException(e);
		}
	}

	public boolean extSourceExists(PerunSession perunSession, ExtSource extSource) throws InternalErrorException {
		Utils.notNull(extSource, "extSource");
		try {
			return 1 == jdbc.queryForInt("select 1 from ext_sources where id=?", extSource.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkExtSourceExists(PerunSession perunSession, ExtSource es) throws InternalErrorException, ExtSourceNotExistsException {
		if(!extSourceExists(perunSession, es)) throw new ExtSourceNotExistsException("ExtSource: " + es);
	}
}
