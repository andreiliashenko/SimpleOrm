package com.anli.simpleorm.test;

public class MockMySqlQueries {

    public static final String SELECT_ROOT_BY_PRIMARY_KEY = "select root by key = ?";
    public static final String SELECT_ROOT_EXISTENT_KEYS_MAIN = "select rootkey by key in ";
    public static final String SELECT_ATOMIC_SET = "select atomicset by foreignKey = ?";
    public static final String SELECT_ATOMIC_BY_KEYS_MAIN = "select atomic by keys in ";
    public static final String SELECT_ATOMIC_EXISTENT_KEYS_MAIN = "select atomickey by key in ";

    public static final String INSERT_ANEMIC_ROOT = "insert anemic root (?)";
    public static final String INSERT_ANEMIC_SUPER = "insert anemic super (?)";
    public static final String UPDATE_SUPER = "update super ?, ?";
    public static final String DELETE_ROOT = "delete root ?";

    public static final String LINK_ATOMIC_LIST_MAIN = "link atomic list for ";
    public static final String UNLINK_ATOMIC_LIST_MAIN = "unlink atomic list for ";
    public static final String CLEAR_ATOMIC_LIST = "clear atomic list";

    public static final String LINK_ATOMIC_SET_MAIN = "link atomic set for ";
    public static final String UNLINK_ATOMIC_SET_MAIN = "unlink atomic set for ";
    public static final String CLEAR_ATOMIC_SET = "clear atomic set";

    public static final String LIST_MACRO = "(${list})";
    public static final String ORDERING_MACRO = "${ordering}";
}
