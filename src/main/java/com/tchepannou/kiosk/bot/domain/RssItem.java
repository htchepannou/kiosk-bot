package com.tchepannou.kiosk.bot.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RssItem {
    //-- Attributes
    private String link;
    private String title;
    private String description;
    private List<String> categories = new ArrayList<>();
    private String language;
    private String country;
    private Date publishedDate;
    private Date creationDate = new Date ();

    //-- Public
    public void addCategory(String category) {
        categories.add(category);
    }


    //-- Getter/Setter
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(final List<String> categories) {
        this.categories = categories;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
