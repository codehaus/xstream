package com.thoughtworks.xstream.converters.extended;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.basic.AbstractBasicConverter;

/**
 * A DateConverter conforming to the ISO8601 standard.
 * http://www.iso.ch/iso/en/CatalogueDetailPage.CatalogueDetail?CSNUMBER=26780
 * 
 * @author Mauro Talevi
 */
public class ISO8601DateConverter extends AbstractBasicConverter{
	private DateTimeFormatter[] formatters;
		
	public ISO8601DateConverter(){
		this.formatters = createISOFormatters();
	}
	
	public boolean canConvert(Class type) {
        return type.equals(Date.class);
    }

    protected Object fromString(String str) {
        for (int i = 0; i < formatters.length; i++) {
        		DateTimeFormatter formatter = formatters[i];
			try {
				DateTime dt = formatter.parseDateTime(str);
				return dt.toDate();
			} catch (IllegalArgumentException e) {
				// try with next formatter
			}
		}
		throw new ConversionException("Cannot parse date " + str);
    }
    
    protected String toString(Object obj) {
    		DateTime dt = new DateTime(obj);
        return dt.toString();
    }

	private DateTimeFormatter[] createISOFormatters() {
		List isoFormatters = new Vector();
		isoFormatters.add(ISODateTimeFormat.dateTime());
		isoFormatters.add(ISODateTimeFormat.dateTimeNoMillis());
		isoFormatters.add(ISODateTimeFormat.basicDate());
		isoFormatters.add(ISODateTimeFormat.basicDateTime());
		isoFormatters.add(ISODateTimeFormat.basicDateTimeNoMillis());
		isoFormatters.add(ISODateTimeFormat.basicTime());
		isoFormatters.add(ISODateTimeFormat.basicTimeNoMillis());
		isoFormatters.add(ISODateTimeFormat.basicTTime());
		isoFormatters.add(ISODateTimeFormat.basicTTimeNoMillis());
		isoFormatters.add(ISODateTimeFormat.basicWeekDate());
		isoFormatters.add(ISODateTimeFormat.basicWeekDateTime());
		isoFormatters.add(ISODateTimeFormat.basicWeekDateTimeNoMillis());
		isoFormatters.add(ISODateTimeFormat.date());
		isoFormatters.add(ISODateTimeFormat.dateHour());
		isoFormatters.add(ISODateTimeFormat.dateHourMinute());
		isoFormatters.add(ISODateTimeFormat.dateHourMinuteSecond());
		isoFormatters.add(ISODateTimeFormat.dateHourMinuteSecondFraction());
		isoFormatters.add(ISODateTimeFormat.dateHourMinuteSecondMillis());
		isoFormatters.add(ISODateTimeFormat.hour());
		isoFormatters.add(ISODateTimeFormat.hourMinute());
		isoFormatters.add(ISODateTimeFormat.hourMinuteSecond());
		isoFormatters.add(ISODateTimeFormat.hourMinuteSecondFraction());
		isoFormatters.add(ISODateTimeFormat.hourMinuteSecondMillis());
		isoFormatters.add(ISODateTimeFormat.time());
		isoFormatters.add(ISODateTimeFormat.timeNoMillis());
		isoFormatters.add(ISODateTimeFormat.tTime());
		isoFormatters.add(ISODateTimeFormat.tTimeNoMillis());
		isoFormatters.add(ISODateTimeFormat.weekDate());
		isoFormatters.add(ISODateTimeFormat.weekDateTime());
		isoFormatters.add(ISODateTimeFormat.weekDateTimeNoMillis());
		isoFormatters.add(ISODateTimeFormat.weekyear());
		isoFormatters.add(ISODateTimeFormat.weekyearWeek());
		isoFormatters.add(ISODateTimeFormat.weekyearWeekDay());
		return (DateTimeFormatter[])isoFormatters.toArray(new DateTimeFormatter[isoFormatters.size()]);
	}
}
