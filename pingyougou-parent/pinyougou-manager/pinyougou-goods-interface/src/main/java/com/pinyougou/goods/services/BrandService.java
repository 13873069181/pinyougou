package com.pinyougou.goods.services;


import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口
 */
public interface BrandService {

    public List<TbBrand> getAllBrand();

    /**
     * 品牌分页
     * @param pageNum //当前页
     * @param pageSize  //每页记录数
     * @return
     */
    public PageResult findPage(int pageNum, int pageSize);

    public void add(TbBrand brand);

    /**
     * 根据id查询实体
     * @param id
     * @return
     */
    public TbBrand findOne(Long id);

    public void update(TbBrand brand);

    /**
     * 删除
     * @param ids
     */
    public void delete(Long[] ids);

    public PageResult findPage(TbBrand brand, int pageNum, int pageSize);

    /**
     * 返回下拉列表
     * @return
     */
    List<Map> selectOptionList();
}
