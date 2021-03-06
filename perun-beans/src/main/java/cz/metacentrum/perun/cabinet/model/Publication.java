package cz.metacentrum.perun.cabinet.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class represents single Publication.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Publication implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Author> authors = new ArrayList<Author>();
	private Double rank;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column PUBLICATION.id
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	private Integer id;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column PUBLICATION.externalId
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	private Integer externalId;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column PUBLICATION.publicationSystemId
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	private Integer publicationSystemId;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column PUBLICATION.title
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	private String title;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column PUBLICATION.year
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	private Integer year;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column PUBLICATION.main
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	private String main;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column PUBLICATION.isbn
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	private String isbn;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column PUBLICATION.categoryId
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	private Integer categoryId;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column PUBLICATION.createdBy
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	private String createdBy;

	private Integer createdByUid;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column PUBLICATION.createdDate
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	private Date createdDate;

	private String doi;

	private Boolean locked;

	public Publication() {

	}

	public Publication(List<Author> authors, Double rank, Integer id,
			Integer externalId, Integer publicationSystemId, String title,
			Integer year, String main, String isbn, Integer categoryId,
			String createdBy, Date createdDate, String doi, Boolean locked) {
		super();
		this.authors = authors;
		this.rank = rank;
		this.id = id;
		this.externalId = externalId;
		this.publicationSystemId = publicationSystemId;
		this.title = title;
		this.year = year;
		this.main = main;
		this.isbn = isbn;
		this.categoryId = categoryId;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.doi = doi;
		this.locked = locked;
	}

	public Publication(List<Author> authors, Double rank, Integer id,
			Integer externalId, Integer publicationSystemId, String title,
			Integer year, String main, String isbn, Integer categoryId,
			String createdBy, Date createdDate, String doi, Boolean locked, Integer createdByUid) {
		this(authors, rank, id, externalId, publicationSystemId, title,
				year, main, isbn, categoryId, createdBy, createdDate, doi, locked);
		this.createdByUid = createdByUid;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column PUBLICATION.id
	 *
	 * @return the value of PUBLICATION.id
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column PUBLICATION.id
	 *
	 * @param id
	 *            the value for PUBLICATION.id
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column PUBLICATION.externalId
	 *
	 * @return the value of PUBLICATION.externalId
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public Integer getExternalId() {
		return externalId;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column PUBLICATION.externalId
	 *
	 * @param externalId
	 *            the value for PUBLICATION.externalId
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public void setExternalId(Integer externalId) {
		this.externalId = externalId;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column PUBLICATION.publicationSystemId
	 *
	 * @return the value of PUBLICATION.publicationSystemId
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public Integer getPublicationSystemId() {
		return publicationSystemId;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column PUBLICATION.publicationSystemId
	 *
	 * @param publicationSystemId
	 *            the value for PUBLICATION.publicationSystemId
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public void setPublicationSystemId(Integer publicationSystemId) {
		this.publicationSystemId = publicationSystemId;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column PUBLICATION.title
	 *
	 * @return the value of PUBLICATION.title
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column PUBLICATION.title
	 *
	 * @param title
	 *            the value for PUBLICATION.title
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public void setTitle(String title) {
		this.title = title == null ? null : title.trim();
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column PUBLICATION.year
	 *
	 * @return the value of PUBLICATION.year
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column PUBLICATION.year
	 *
	 * @param year
	 *            the value for PUBLICATION.year
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public void setYear(Integer year) {
		this.year = year;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column PUBLICATION.main
	 *
	 * @return the value of PUBLICATION.main
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public String getMain() {
		return main;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column PUBLICATION.main
	 *
	 * @param main
	 *            the value for PUBLICATION.main
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public void setMain(String main) {
		this.main = main == null ? null : main.trim();
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column PUBLICATION.isbn
	 *
	 * @return the value of PUBLICATION.isbn
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public String getIsbn() {
		return isbn;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column PUBLICATION.isbn
	 *
	 * @param isbn
	 *            the value for PUBLICATION.isbn
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public void setIsbn(String isbn) {
		this.isbn = isbn == null ? null : isbn.trim();
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column PUBLICATION.categoryId
	 *
	 * @return the value of PUBLICATION.categoryId
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public Integer getCategoryId() {
		return categoryId;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column PUBLICATION.categoryId
	 *
	 * @param categoryId
	 *            the value for PUBLICATION.categoryId
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * Return rank for publication
	 *
	 * @return rank of publication (value of PUBLICATION.rank)
	 */
	public Double getRank() {
		return rank;
	}

	/**
	 * Sets publication's rank
	 *
	 * @param rank value for PUBLICATION.rank
	 */
	public void setRank(Double rank) {
		this.rank = rank;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column PUBLICATION.createdBy
	 *
	 * @return the value of PUBLICATION.createdBy
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column PUBLICATION.createdBy
	 *
	 * @param createdBy
	 *            the value for PUBLICATION.createdBy
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column PUBLICATION.createdDate
	 *
	 * @return the value of PUBLICATION.createdDate
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column PUBLICATION.createdDate
	 *
	 * @param createdDate
	 *            the value for PUBLICATION.createdDate
	 *
	 * @mbggenerated Fri Nov 04 19:27:43 CET 2011
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getCreatedByUid() {
		return createdByUid;
	}

	public void setCreatedByUid(Integer createdByUid) {
		this.createdByUid = createdByUid;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

	public String getBeanName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Two publications are equal if id property is the same (but not null)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Publication other = (Publication) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (id.equals(other.id))
			return true;

		return false;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[id=").append(id).append(", externalId=").append(externalId).append(", pubSysId=").append(publicationSystemId).append(", title=").append(title).append(", categoryId=").append(categoryId).append(", year=").append(year).append(", isbn=").append(isbn).append(", doi=").append(doi).append(", locked=").append(locked).append(", main=").append(main).append(", createdBy=").append(createdBy).append(", createdByUid=").append(createdByUid).append(", createdDate=").append(createdDate).append(", rank=").append(rank).append(", authors=").append(authors).append("]").toString();
	}

	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[" ).append(
			"id=<").append(getId()).append(">" ).append(
			", externalIdId=<").append(getExternalId()).append(">" ).append(
			", pubSysId=<").append(getPublicationSystemId()).append(">" ).append(
			", title=<").append(getTitle()).append(">" ).append(
			", categoryId=<").append(getCategoryId()).append(">" ).append(
			", year=<").append(getYear()).append(">" ).append(
			", isbn=<").append(getIsbn()).append(">" ).append(
			", doi=<").append(getDoi()).append(">" ).append(
			", locked=<").append(getLocked()).append(">" ).append(
			", main=<").append(getMain()).append(">" ).append(
			", rank=<").append(getRank()).append(">" ).append(
			", authors=<").append(getAuthors()).append(">" ).append(
			", createdBy=<").append(getCreatedBy()).append(">" ).append(
			", createdByUid=<").append(getCreatedByUid()).append(">" ).append(
			", createdDate=<").append(getCreatedDate()).append(">" ).append(
			']').toString();
	}

}
