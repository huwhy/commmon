package cn.huwhy.interfaces;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Term implements Serializable {
    /**
     * 分页数
     */
    private Long                size     = 15L;
    /**
     * 查询页
     */
    private Long                page     = 1L;
    /**
     * 总记录数
     */
    private Long                total    = 0L;
    /**
     * 返回总记录数
     */
    private Boolean             hasTotal = true;
    /**
     * 优化分页参数
     */
    private Boolean             hasStart = false;
    /**
     * 排序
     */
    private Map<String, Sort>   sorts    = new LinkedHashMap<>();
    /**
     * 扩展参数
     */
    private Map<String, Object> args     = new LinkedHashMap<>();

    public Map<String, Object> getArgs() {
        return args;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getPage() {
        return page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getTotalPage() {
        Long totalPage = getTotal() / getSize();
        if (getTotal() % this.getSize() > 0) {
            totalPage += 1;
        }
        return totalPage;
    }

    public Boolean getHasTotal() {
        return hasTotal;
    }

    public void setHasTotal(Boolean hasTotal) {
        this.hasTotal = hasTotal;
    }

    public Boolean getHasStart() {
        return hasStart;
    }

    public void setHasStart(Boolean hasStart) {
        this.hasStart = hasStart;
    }

    public Long getStart() {
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
