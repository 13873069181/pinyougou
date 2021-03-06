package com.pinyougou.goods.services;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;


import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");  //状态，未审核
		int insert = goodsMapper.insert(goods.getGoods());//插入商品的基本信息
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());  //将商品的基本表的ID给商品扩展表
		int insert1 = goodsDescMapper.insert(goods.getGoodsDesc());//插入商品的扩展信息
		System.out.println("商品插入："+insert+"    扩展插入："+insert1);

		saveItemList(goods);  //插入SKU数据

	}

	public void setItemValues(TbItem item, Goods goods) {
		//商品分类
		item.setCategoryid(goods.getGoods().getCategory3Id());   //三级分类ID
		item.setCreateTime(new Date());  //创建时间
		item.setUpdateTime(new Date());  //更新时间

		item.setGoodsId(goods.getGoods().getId());  //商品ID
		item.setSellerId(goods.getGoods().getSellerId());  //商家ID

		//分类名称
		TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(tbItemCat.getName());

		//品牌名称
		TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(tbBrand.getName());

		//商家名称（店铺名称）
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());

		//图片
		List<Map> mapList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (mapList.size() > 0 ) {
			item.setImage((String)mapList.get(0).get("url"));
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//更新基本表数据
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//更新扩展表数据
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//删除原有的SKU列表数据
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		saveItemList(goods);  //插入SKU数据

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		//商品基本表
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//商品描述
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);

		//读取SKU列表
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);
        return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//物理删除
//			goodsMapper.deleteByPrimaryKey(id);
			//逻辑删除
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);  //表示逻辑删除

		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();  //指定条件为未逻辑删除记录
		if(goods!=null){
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
//				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);

		}
	}

	/**
	 * 根据SPU的ID集合查询SKU列表
	 * @param goodsIds
	 * @param status
	 * @return
	 */
	@Override
	public List<TbItem> findItemListByGoodsListAndStatus(Long[] goodsIds, String status) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));  //指定条件：SPU ID集合
		criteria.andStatusEqualTo(status);  //状态
        List<TbItem> list = itemMapper.selectByExample(example);
        return list;
	}

	private void saveItemList(Goods goods) {
		if ("1".equals(goods.getGoods().getIsEnableSpec())) {
			for (TbItem item : goods.getItemList()) {
				//创建标题：SPU名称+规格选项值
				String title = goods.getGoods().getGoodsName();  //SPU名称
				Map<String, Object> map = JSON.parseObject(item.getSpec());
				for (String key :
						map.keySet()) {
					title += " " + map.get(key);
				}
				item.setTitle(title);
				setItemValues(item, goods);
				itemMapper.insert(item);
			}
		} else {
			//如果没有启用规格
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());  //标题
			item.setStatus("1");  //状态
			item.setPrice(goods.getGoods().getPrice());  //价格
			item.setNum(9999);  //库存
			item.setIsDefault("1");  //默认
			setItemValues(item, goods);
			item.setSpec("{}");  //规格
			itemMapper.insert(item);
		}
	}
	
}
