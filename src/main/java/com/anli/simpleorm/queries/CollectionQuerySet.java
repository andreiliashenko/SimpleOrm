package com.anli.simpleorm.queries;

public class CollectionQuerySet {

    protected QueryDescriptor selectCollectionKeysQuery;
    protected QueryDescriptor linkCollectionQuery;
    protected QueryDescriptor clearCollectionQuery;
    protected QueryDescriptor unlinkCollectionQuery;

    public QueryDescriptor getSelectCollectionKeysQuery() {
        return selectCollectionKeysQuery;
    }

    public void setSelectCollectionKeysQuery(QueryDescriptor selectCollectionKeysQuery) {
        this.selectCollectionKeysQuery = selectCollectionKeysQuery;
    }

    public QueryDescriptor getLinkCollectionQuery() {
        return linkCollectionQuery;
    }

    public void setLinkCollectionQuery(QueryDescriptor linkCollectionQuery) {
        this.linkCollectionQuery = linkCollectionQuery;
    }

    public QueryDescriptor getClearCollectionQuery() {
        return clearCollectionQuery;
    }

    public void setClearCollectionQuery(QueryDescriptor clearCollectionQuery) {
        this.clearCollectionQuery = clearCollectionQuery;
    }

    public QueryDescriptor getUnlinkCollectionQuery() {
        return unlinkCollectionQuery;
    }

    public void setUnlinkCollectionQuery(QueryDescriptor unlinkCollectionQuery) {
        this.unlinkCollectionQuery = unlinkCollectionQuery;
    }
}
