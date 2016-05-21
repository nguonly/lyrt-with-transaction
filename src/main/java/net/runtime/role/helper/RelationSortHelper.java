package net.runtime.role.helper;


import net.runtime.role.orm.Relation;

import java.util.Comparator;

/**
 * Created by nguonly on 7/30/15.
 */
public class RelationSortHelper {
    public static Comparator<Relation> SEQUENCE_DESC = (s1, s2) -> Long.compare(s2.getSequence(), s1.getSequence());

    public static Comparator<Relation> TYPE_DESC = (s1, s2) -> Long.compare(s2.getType(), s1.getType());
}
