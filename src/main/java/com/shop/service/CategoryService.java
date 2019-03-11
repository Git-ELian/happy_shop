package com.shop.service;

import com.shop.common.ServerResponse;
import com.shop.pojo.Category;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author elian
 * @create 2019-03-11 15:53
 * @desc 分类接口
 **/

public interface CategoryService {
    ServerResponse addCategory(String categotyName, Integer parentId);

    ServerResponse updateCategoryName(Integer categoryId, String categotyName);

    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);

}
