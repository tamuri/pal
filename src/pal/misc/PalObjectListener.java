// PalObjectListener.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * Defines objects that monitor certain PalObjects for changes
 *
 * @version $Id: PalObjectListener.java,v 1.2 2002/07/09 06:30:59 matt Exp $
 *
 * @author Matthew Goode
 */


public interface PalObjectListener {
	/** Signifies that parametric valued governing the state of this Object have changed.
	 *  Parameters don't have to be visible ones
	 *  For example, in trees the branch lengths have changed.
	 */
	void parametersChanged(PalObjectEvent pe);
	/** Signifies that the structure of the object has changed (for example in trees to topology
	 *  has changed)
	 */
	void structureChanged(PalObjectEvent pe);

	/**
	 * A base class for classes that fire PalObject events
	 */
	abstract public static class  EventGenerator {

		private transient PalObjectListener listeners_ = null;

		private transient PalObjectEvent defaultPalEvent_ = null;

		public void addPalObjectListener(PalObjectListener pol) {
			listeners_ = PalEventMulticaster.add(listeners_,pol);
		}
		public void removePalObjectListener(PalObjectListener pol) {
			listeners_ = PalEventMulticaster.remove(listeners_,pol);
		}
		/**
		 * Called by subclasses to fire the default Event on all listeners
		 */
		protected void fireParametersChangedEvent() {
			if(listeners_!=null) {
				if(defaultPalEvent_==null) {
					defaultPalEvent_ = new PalObjectEvent(this);
				}
				listeners_.parametersChanged(defaultPalEvent_);
			}
		}

		/**
		 * Called by subclasses to fire a specific PalObjectEvent on all listeners
		 */
		protected void fireParametersChangedEvent(PalObjectEvent pe) {
			if(listeners_!=null) {
				listeners_.parametersChanged(pe);
			}
		}
		/**
		 * Called by subclasses to fire the default Event on all listeners
		 */
		protected void fireStructureChangedEvent() {
			if(listeners_!=null) {
				if(defaultPalEvent_==null) {
					defaultPalEvent_ = new PalObjectEvent(this);
				}
				listeners_.structureChanged(defaultPalEvent_);
			}
		}
		/**
		 * Called by subclasses to fire a specific PalObjectEvent on all listeners
		 */
		protected void fireStructureChangedEvent(PalObjectEvent pe) {
			if(listeners_!=null) {
				listeners_.structureChanged(pe);
			}
		}
	}
}