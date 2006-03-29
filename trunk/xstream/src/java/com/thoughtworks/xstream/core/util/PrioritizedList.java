package com.thoughtworks.xstream.core.util;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * List that allows items to be added with a priority that will affect the order in which they are later iterated over.
 *
 * Objects with a high priority will appear before objects with a low priority in the list. If two objects of the same
 * priority are added to the list, the most recently added one will be iterated over first.
 *
 * Implementation uses a TreeSet, which has a garanteed add time of O(log(n)).
 * 
 * @author Joe Walnes
 * @author Guilherme Silveira
 */
public class PrioritizedList {
	
    private final Set set = new TreeSet();

    private int lowestPriority = Integer.MAX_VALUE;
    
    private int lastId = 0;

    /**
     * Add an item with a default Prioritized of zero.
     */
    public void add(Object item) {
        add(item, 0);
    }

    public void add(Object item, int priority) {
    	if (this.lowestPriority > priority) {
			this.lowestPriority = priority;
		}
        this.set.add(new PrioritizedItem(item, priority, ++lastId));
    }

    public Iterator iterator() {
        return new PrioritizedItemIterator(this.set.iterator());
    }

    public Object firstOfLowestPriority() {
        for (Iterator it = set.iterator(); it.hasNext();) {
			PrioritizedItem item = (PrioritizedItem) it.next();
			if (item.priority == lowestPriority) {
				return item.value;
			}
		}
        return null;
    }

    private static class PrioritizedItem implements Comparable {

        final Object value;
        final int priority;
		final int id;

        public PrioritizedItem(Object value, int Prioritized, int id) {
            this.value = value;
            this.priority = Prioritized;
            this.id = id;
        }

		public int compareTo(Object o) {
			PrioritizedItem other = (PrioritizedItem) o;
			if (this.priority != other.priority) {
				return (other.priority - this.priority);
			}
			return (other.id - this.id);
		}
		
		public boolean equals(Object obj) {
			return this.id == ((PrioritizedItem) obj).id; 
		}

    }

    private static class PrioritizedItemIterator implements Iterator {

	private Iterator iterator;

        public PrioritizedItemIterator(Iterator iterator) {
            this.iterator = iterator;
        }

        public void remove() {
            // call iterator.remove()?
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            return ((PrioritizedItem)iterator.next()).value;
        }

    }

}
