/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package de.ifgi.airbase.feeder.io.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;

import org.joda.time.DateTime;
import org.joda.time.Period;

import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.util.Utils;

public class TimeRangeFilter implements FileFilter {

    private LinkedList<Range> ranges = new LinkedList<Range>();

    public void addStartEndRange(DateTime start, DateTime end) {
        this.ranges.add(new Range(start, end));
    }

    public void addStart(DateTime start) {
        addStartEndRange(start, null);
    }

    public void addEnd(DateTime end) {
        addStartEndRange(null, end);
    }

    public void addRange(DateTime start, Period length) {
        this.ranges.add(new Range(start, start.plus(length)));
    }

    public void addRange(Period length, DateTime end) {
        this.ranges.add(new Range(end.minus(length), end));
    }

    public boolean accept(EEAMeasurement eeams) {
        return accept(eeams.getTime());
    }

    public boolean accept(DateTime dt) {
        for (Range r : this.ranges) {
            if (r.accept(dt)) {
                return true;
            }
        }
        return false;
    }

    private boolean accept(Range range) {
    	for (Range r : this.ranges) {
    		if (overlap(range,r)) {
   				return true;
    		}
    	}
    	return false;
    }
    
    private boolean overlap(Range r1, Range r2) {
        if (r1 == null || r2 == null) {
            throw new NullPointerException();
        }
    	if (r1.start == null) { 
    		if (r2.start == null) {
        		return true;
        	}
       		return !r1.end.isBefore(r2.start);
    	}
    	if (r1.end == null) {
    		if (r2.end == null) {
    			return true;
    		}
    		return !r2.end.isBefore(r1.start);
    	}
    	if (r2.start == null) {
       		return !r2.end.isBefore(r1.start);
    	}
    	if (r2.end == null) {
    		return !r1.end.isBefore(r2.start);
    	}
    	
    	return !(r1.end.isBefore(r2.start) || r2.end.isBefore(r1.start));
    }
    
    @Override
    public boolean accept(File pathname) {
        if (pathname.isFile()) {
            String split[] = pathname.getName().split("\\.");
            DateTime start = Utils.parseDate(split[1]);
            DateTime end = Utils.parseDate(split[2]);
            return accept(new Range(start,end));
        }
        return false;
    }

    private class Range {
        private DateTime start, end;

        Range(DateTime start, DateTime end) {
            this.start = start;
            this.end = end;
        }

        public boolean accept(DateTime dt) {
            if (this.start == null) {
                if (this.end == null) {
                    throw new NullPointerException();
                }
                return dt.isBefore(this.end) || dt.isEqual(this.end);
            }
            if (this.end == null) {
                return dt.isAfter(this.start) || dt.isEqual(this.start);
            }
            return (dt.isAfter(this.start) || dt.isEqual(this.start))
                   && (dt.isBefore(this.end) || dt.isEqual(this.end));
        }
    }

    
    
}
