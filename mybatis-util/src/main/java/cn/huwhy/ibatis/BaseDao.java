package cn.huwhy.ibatis;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.huwhy.interfaces.Term;

public interface BaseDao<T, PK extends Serializable> {

    /**
     * 保存
     *
     * @param po
     * @return
     */
    int save(T po);

    /**
     * 批量保存
     *
     * @param list
     * @return
     */
    int saves(@Param("list") Collection<T> list);

    /**
     * 更新
     *
     * @param po
     * @return
     */
    int update(T po);

    /**
     * 批量更新
     *
     * @param pos
     * @return
     */
    int updates(@Param("list") Collection<T> pos);

    /**
     * 根据主键获取
     *
     * @param id
     * @return
     */
    T get(PK id);

    /**
     * 悲欢锁  实现sql 请带上 for update： select * from a where id=1 for update
     *
     * @param id
     * @return
     */
    T lock(PK id);

    /**
     * 根据term 查询列表
     *
     * @param term
     * @return
     */
    List<T> findByTerm(Term term);

    /**
     * 分页查询列表
     *
     * @param term
     * @return
     */
    List<T> findPaging(Term term);

    /**
     * 下一个序列值
     * 具体请查看readme
     *
     * @return
     */
    Long nextId();
}
