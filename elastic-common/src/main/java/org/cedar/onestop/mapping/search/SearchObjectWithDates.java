package org.cedar.onestop.mapping.search;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
// import org.cedar.onestop.mapping.Link;

public interface SearchObjectWithDates {

    public String getBeginDate();
    public void setBeginDate(String beginDate);
    public SearchObjectWithDates withBeginDate(String beginDate);

    public Long getBeginYear();
    public void setBeginYear(Long beginYear);
    public SearchObjectWithDates withBeginYear(Long beginYear);

    public Short getBeginDayOfYear();
    public void setBeginDayOfYear(Short beginDayOfYear);
    public SearchObjectWithDates withBeginDayOfYear(Short beginDayOfYear);

    public Byte getBeginDayOfMonth();
    public void setBeginDayOfMonth(Byte beginDayOfMonth);
    public SearchObjectWithDates withBeginDayOfMonth(Byte beginDayOfMonth);

    public Byte getBeginMonth();
    public void setBeginMonth(Byte beginMonth);
    public SearchObjectWithDates withBeginMonth(Byte beginMonth);

    public String getEndDate();
    public void setEndDate(String endDate);
    public SearchObjectWithDates withEndDate(String endDate);

    public Long getEndYear();
    public void setEndYear(Long endYear);
    public SearchObjectWithDates withEndYear(Long endYear);

    public Short getEndDayOfYear();
    public void setEndDayOfYear(Short endDayOfYear);
    public SearchObjectWithDates withEndDayOfYear(Short endDayOfYear);

    public Byte getEndDayOfMonth();
    public void setEndDayOfMonth(Byte endDayOfMonth);
    public SearchObjectWithDates withEndDayOfMonth(Byte endDayOfMonth);

    public Byte getEndMonth();
    public void setEndMonth(Byte endMonth);
    public SearchObjectWithDates withEndMonth(Byte endMonth);



}
