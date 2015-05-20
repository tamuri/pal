// PalObjectEvent.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)
package pal.misc;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for accomodating Pal listeners
 *
 * @author Matthew Goode
 * @version $Id: PalEventMulticaster.java,v 1.1 2001/10/10 07:50:02 matt Exp $
 */
public class PalEventMulticaster {
    /**
     * For managing PalObjectListeners
     * Usage: PalObjectListener current = null;
     * ... <emph>Adding</emph> <br>
     * current = PalEventMultiCaster.add(current, toAdd); <br>
     * ... Usage <br>
     * <code>
     * if(current!=null) {
     * current.structureChanged(pe);
     * }
     * </code>
     * ... Removal <br>
     * current = PalEventMultiCaster.remove(current,toAdd);
     *
     * @see #remove
     */
    public static final PalObjectListener add(PalObjectListener old, PalObjectListener toAdd) {
        if (old == null) {
            return toAdd;
        }
        if (old instanceof POLMulti) {
            ((POLMulti) old).add(toAdd);
            return old;
        }
        if (toAdd instanceof POLMulti) {
            ((POLMulti) toAdd).add(old);
            return toAdd;
        }
        return new POLMulti(old, toAdd);
    }

    /**
     * For managing PalObjectListeners
     * Usage: PalObjectListener current = null;
     * ... Adding <br>
     * current = PalEventMultiCaster.add(current, toAdd); <br>
     * ... Usage <br>
     * <code>
     * if(current!=null) {
     * current.structureChanged(pe);
     * }
     * </code>
     * <p>
     * ... <emph>Removal</emph> <br>
     * current = PalEventMultiCaster.remove(current,toAdd);
     *
     * @see #add
     */
    public static final PalObjectListener remove(PalObjectListener old, PalObjectListener toRemove) {
        if (old == toRemove) {
            return null;
        }
        if (old instanceof POLMulti) {
            return ((POLMulti) old).remove(toRemove);
        }
        return old;
    }

    /**
     * For managing ExternalParamterListeners
     * Usage: ExternalParamterListener current = null;
     * ... <emph>Adding</emph> <br>
     * current = PalEventMultiCaster.add(current, toAdd); <br>
     * ... Usage <br>
     * <code>
     * if(current!=null ) {
     * current.structureChanged(pe);
     * }
     * </code>
     * ... Removal <br>
     * current = PalEventMultiCaster.remove(current,toAdd);
     *
     * @see #remove
     */
    public static final ExternalParameterListener add(ExternalParameterListener old, ExternalParameterListener toAdd) {
        if (old == null) {
            return toAdd;
        }
        if (old instanceof EPLMulti) {
            ((EPLMulti) old).add(toAdd);
            return old;
        }
        if (toAdd instanceof EPLMulti) {
            ((EPLMulti) toAdd).add(old);
            return toAdd;
        }
        return new EPLMulti(old, toAdd);
    }

    /**
     * For managing ExternalParamterListeners
     * Usage: ExternalParamterListener current = null;
     * ... Adding <br>
     * current = PalEventMultiCaster.add(current, toAdd); <br>
     * ... Usage <br>
     * current.structureChanged(pe); <br>
     * ... <emph>Removal</emph> <br>
     * current = PalEventMultiCaster.remove(current,toAdd);
     *
     * @see #add
     */
    public static final ExternalParameterListener remove(ExternalParameterListener old, ExternalParameterListener toRemove) {
        if (old == toRemove) {
            return null;
        }
        if (old instanceof EPLMulti) {
            return ((EPLMulti) old).remove(toRemove);
        }
        return old;
    }

//==========================================================================================

    private static class POLMulti implements PalObjectListener {
        List<PalObjectListener> v = new ArrayList<>(10);

        public POLMulti(PalObjectListener polOne, PalObjectListener polTwo) {
            v.add(polOne);
            v.add(polTwo);
        }

        public void add(PalObjectListener toAdd) {
            v.add(toAdd);
        }

        public PalObjectListener remove(PalObjectListener toRemove) {
            v.remove(toRemove);
            if (v.size() == 1) {
                return v.get(0);
            }
            return this;
        }

        public void parametersChanged(PalObjectEvent pe) {
            for (int i = 0; i < v.size(); i++) {
                (v.get(i)).parametersChanged(pe);
            }
        }

        public void structureChanged(PalObjectEvent pe) {
            for (int i = 0; i < v.size(); i++) {
                (v.get(i)).structureChanged(pe);
            }
        }
    }

    private static class EPLMulti implements ExternalParameterListener {
        List<ExternalParameterListener> v = new ArrayList<>(10);

        public EPLMulti(ExternalParameterListener eplOne, ExternalParameterListener eplTwo) {
            v.add(eplOne);
            v.add(eplTwo);
        }

        public void add(ExternalParameterListener toAdd) {
            v.add(toAdd);
        }

        public ExternalParameterListener remove(ExternalParameterListener toRemove) {
            v.remove(toRemove);
            if (v.size() == 1) {
                return v.get(0);
            }
            return this;
        }

        public void parameterChanged(ParameterEvent pe) {
            for (int i = 0; i < v.size(); i++) {
                (v.get(i)).parameterChanged(pe);
            }
        }
    }
}