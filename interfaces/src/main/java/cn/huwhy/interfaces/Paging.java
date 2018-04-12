package cn.huwhy.interfaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Paging<T> implements Serializable {
    public static Paging empty = new Paging<>();

    /**
     * 数据
     */
    private List<T> data;
    private Term    term;

    public Paging() {
        this.term = new Term() {};
    }

    public Paging(Term term) {
        this.term = term;
    }

    public Paging(Term term, List<T> data) {
        this.term = term;
        this.data = data;
    }

    public static Paging empty(Term term) {
        Paging paging = new Paging(term);
        paging.setData(new ArrayList<>(0));
        return paging;
    }

    public long getTotal() {
        return term.getTotal();
    }

    public long getSize() {
        return term.getSize();
    }

    public long getPage() {
        return term.getPage();
    }

    public long getTotalPage() {
        return term.getTotalPage();
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }
}
