// LableMapping.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * Title:        LabelMapping
 * Description:  Allows for the substitution of one label for another
 *
 * @author Matthew Goode
 * @version 1.0
 */

import java.util.HashMap;
import java.util.Map;

public class LabelMapping implements java.io.Serializable {
    Map<String, String> mappings_ = new HashMap<>();

    //
    // Serialization code
    //
    private static final long serialVersionUID = -9217142171228146380L;

    //serialver -classpath ./classes pal.misc.LabelMapping
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.writeByte(1); //Version number
        out.writeObject(mappings_);
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        byte version = in.readByte();
        switch (version) {
            default: {
                mappings_ = (Map<String, String>) in.readObject();
                break;
            }
        }
    }

    private LabelMapping(Map<String, String> mapping) {
        this.mappings_ = mapping;
    }

    private LabelMapping(LabelMapping toCopy) {
        this.mappings_ = new HashMap<>(toCopy.mappings_);
    }

    public LabelMapping() {
    }

    public void addMapping(String id, String label) {
        mappings_.put(id, label);
    }

    public void addMapping(Identifier id, String label) {
        if (id != null && id.getName() != null) {
            mappings_.put(id.getName(), label);
        }
    }

    /**
     * @param ids Names
     * @param labels associated colours
     * @note assumes parallel arrays
     */
    public void addMappings(String[] ids, String[] labels) {
        for (int i = 0; i < ids.length; i++) {
            mappings_.put(ids[i], labels[i]);
        }
    }

    public String getLabel(String id, String defaultLabel) {
        if (id == null || !mappings_.containsKey(id)) {
            return defaultLabel;
        }
        return mappings_.get(id);
    }

    public String getLabel(Identifier id, String defaultLabel) {
        if (id == null) {
            return defaultLabel;
        }
        return getLabel(id.getName(), defaultLabel);
    }

    public String getLabel(Identifier id) {
        return getLabel(id.getName(), id.getName());
    }

    public Identifier getLabelIdentifier(Identifier id) {
        if (id == null) {
            return null;
        }
        return new Identifier(getLabel(id.getName(), id.getName()));
    }

    /**
     * If a mapping occurs more than once will rename instance to "x 1", "x 2"... and so on where x is the mapping in question
     */
    public LabelMapping getUniquifiedMappings() {
        Map<String, Integer> totals = new HashMap<>();

        for (String key : mappings_.keySet()) {
            String mapping = mappings_.get(key);
            int count = 1;
            if (totals.containsKey(mapping)) {
                count = totals.get(mapping) + 1;
            }
            totals.put(mapping, count);
        }

        Map<String, Integer> counts = new HashMap<>();
        Map<String, String> result = new HashMap<>();

        for (String key : mappings_.keySet()) {
            String mapping = mappings_.get(key);

            int total = totals.get(mapping);
            if (total == 1) {
                result.put(key, mapping);
            } else {
                int count = 1;
                if (counts.containsKey(mapping)) {
                    count = counts.get(mapping) + 1;
                }
                counts.put(mapping, count);
                result.put(key, mapping + " " + count);
            }
        }

        return new LabelMapping(result);
    }

    public LabelMapping getRelabeled(Relabeller relabeller) {
        Map<String, String> newMapping = new HashMap<>();
        for (String key : mappings_.keySet()) {
            String old = mappings_.get(key);
            newMapping.put(key, relabeller.getNewLabel(old));
        }
        return new LabelMapping(newMapping);
    }

    public IdGroup getMapped(IdGroup original) {
        String[] oldIDs = Identifier.getNames(original);
        String[] newIDs = new String[oldIDs.length];
        for (int i = 0; i < newIDs.length; i++) {
            newIDs[i] = getLabel(oldIDs[i], oldIDs[i]);
        }
        return new SimpleIdGroup(newIDs);
    }

    // Static classes

    public interface Relabeller {
        String getNewLabel(String oldLabel);
    }
}