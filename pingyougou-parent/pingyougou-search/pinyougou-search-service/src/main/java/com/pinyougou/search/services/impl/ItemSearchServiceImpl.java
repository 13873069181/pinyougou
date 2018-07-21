package com.pinyougou.search.services.impl;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.services.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    //搜索方法
    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap();
//        Query query = new SimpleQuery("*:*");
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        query.addCriteria(criteria);
//        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);
//        map.put("rows", scoredPage.getContent());

        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));  //去掉关键字的空格
        //高亮显示
        //1. 查询列表
        map.putAll(searchList(searchMap));
        //2. 分组查询列表
        List<String> categoryList = searchCagegoryList(searchMap);
        map.put("categoryList", categoryList);

        //3. 查询品牌和规格列表
        String category = (String) searchMap.get("category");
        if (!category.equals("")) {
            //如果商品分类不为空，则按照商品分类来查
            map.putAll(searchBrandAndSpecList(category));
        } else {
            if (categoryList.size() > 0) {
                //商品分类为空，则默认查询第一个
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 根据GoodsId来删除solr中之前存储的索引
     * @param goodsIds  （SPU）
     */
    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);

        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //查询列表（代码改良，相比于list来说，map能存储更多类型）
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        //高亮初始化
        HighlightQuery query = new SimpleHighlightQuery();

        //构建高亮对象
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");  //设置高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");  //设置前缀
        highlightOptions.setSimplePostfix("</em>");  //设置后缀
        query.setHighlightOptions(highlightOptions);  //为查询对象设置高亮选项

        //1.1 关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2 按照商品分类来进行查询
        if (!"".equals(searchMap.get("category"))) {
            //表示已经选择了分类
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteria1 = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(criteria1);
            query.addFilterQuery(filterQuery);
        }

        //1.3 按照品牌来进行查询
        if (!"".equals(searchMap.get("brand"))) {
            //表示已经选择了分类
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteria1 = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(criteria1);
            query.addFilterQuery(filterQuery);
        }

        //1.4 按照规格过滤
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria criteria1 = new Criteria("item_spec_"+key).is(specMap.get(key));
                filterQuery.addCriteria(criteria1);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.5 按照价格过滤
        if (!"".equals(searchMap.get("price"))) {
            String priceStr = (String) searchMap.get("price");
            String[] price = priceStr.split("-");
            if (!price[0].equals("0")) {
                //如果最低价格不大于等于0
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria criteria1 = new Criteria("item_price").greaterThan(price[0]);
                filterQuery.addCriteria(criteria1);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")) {
                //如果最高价格不为无限
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria criteria1 = new Criteria("item_price").lessThan(price[1]);
                filterQuery.addCriteria(criteria1);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6 分页
        Integer pageNo = (Integer) searchMap.get("pageNo");  //获取页码
        if (pageNo == null) {
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");  //获取每页大小
        if (pageSize == null ) {
            pageSize = 20;
        }
        query.setOffset(pageSize*(pageNo -1));  //起始索引
        query.setRows(pageSize);  //每页记录数

        // 1.7 排序
        String sortValue = (String) searchMap.get("sort");  //升序ASC，降序DESC
        String sortField = (String) searchMap.get("sortField");  //排序字段
        if (sortValue != null && !sortValue.equals("")) {
            if (sortValue.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
                query.addSort(sort);
            } else {
                Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
                query.addSort(sort);
            }
        }


        //******************   获取高亮结果集  *********************************
        //高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //高亮入口集合（每条记录的高亮入口）
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList) {
            //获取高亮列表（高亮域的个数）
            List<HighlightEntry.Highlight> highlights = entry.getHighlights();
//            for (HighlightEntry.Highlight hight:highlights ) {
//                List<String> snipplets = hight.getSnipplets();  //每个域可能存储多个值
//
//            }

            if (highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {
                //这里 page.getContent() 和 entry.getEntity() 有相同的引用
                TbItem tbItem = entry.getEntity();
                tbItem.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }

        map.put("rows", page.getContent());
        map.put("totalPages", page.getTotalPages());  //总页数
        map.put("total", page.getTotalElements());  //总记录数
        return map;
    }

    /**
     * 分组查询
     * @param searchMap
     * @return
     */
    private List<String> searchCagegoryList(Map searchMap) {
        List<String> list = new ArrayList<>();

        Query query = new SimpleQuery("*:*");
        //根据关键字进行查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));  //where 。。。。。
        query.addCriteria(criteria);

        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");  //group by ......
        query.setGroupOptions(groupOptions);

        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组查询结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();

        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue());  //将分组的结果添加到返回值中
        }
        return list;

    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 根据分类名称查询品牌和规格列表
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //1. 根据分类名称获取模版Id
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (templateId != null) {
            //2. 根据模版Id获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);
            //3. 根据模版Id获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }


        return map;
    }


}
