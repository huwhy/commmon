package cn.huwhy.interfaces;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Term implements Serializable {
    /**
     * 分页数
     */
    private long size = 15L;
    /**
     * 查询页
     */
    private long page = 1L;
    /**
     * 总记录数
     */
    private long total = 0L;
    /**
     * 返回总记录数
     */
    private boolean hasTotal = true;
    /**
     * 优化分页参数
     */
    private boolean hasStart = false;
    /**
     * 排序
     */
    private Map<String, Sort> sorts = new LinkedHashMap<>();
    /**
     * 扩展参数
     */
    private Map<String, Object> args = new LinkedHashMap<>();

    public Map<String, Object> getArgs() {
        return args;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalPage() {
        long totalPage = getTotal() / getSize();
        if (getTotal() % this.getSize() > 0) {
            totalPage += 1;
        }
        return totalPage;
    }

    public boolean getHasTotal() {
        return hasTotal;
    }

    public void setHasTotal(boolean hasTotal) {
        this.hasTotal = hasTotal;
    }

    public boolean getHasStart() {
        return hasStart;
    }

    public void setHasStart(boolean hasStart) {
        this.hasStart = hasStart;
    }

    public long getStart() {
        return (this.page - 1) * this.size;
    }

    public Map<String, Sort> getSorts() {
        return sorts;
    }

    public void setSorts(Map<String, Sort> sorts) {
        this.sorts = sorts;
    }

    public void addSort(String field, Sort sort) {
        this.sorts.put(field, sort);
    }

    public void addArg(String name, Object value) {
        args.put(name, value);
    }

    public enum Sort {
        ASC,
        DESC;

        public String getValue() {
            return name();
        }
    }
}
