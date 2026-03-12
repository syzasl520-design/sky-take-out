package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.sky.constant.StatusConstant.ENABLE;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper DishMapper;

    @Autowired
    private SetmealMapper SetmealMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void saveSort(CategoryDTO categoryDTO) {
        Category category = new Category();
        // 对象属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);
        // 分类状态默认设置为禁用状态0
        category.setStatus(StatusConstant.DISABLE);

        // 设置创建时间、修改时间、创建人、修改人
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.insert(category);
    }

    /**
     * 分类分页查询
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 开始分页查询
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);

        long total = page.getTotal();
        List<Category> records = page.getResult();
        PageResult pageResult = new PageResult(total, records);

        return pageResult;
    }

    /*
    * 启用、禁用分类
    * */
    @Override
    public void startOrStop(Integer status, Long id) {
        //根据分类ID修改分类状态status
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();

        categoryMapper.update(category);
    }

    /**
     * 删除分类
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        Integer count = DishMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new RuntimeException("当前分类下关联了菜品，不能删除");
        }

        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = SetmealMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有套餐，不能删除
            throw new RuntimeException("当前分类下关联了套餐，不能删除");
        }
        categoryMapper.deleteById(id);
    }

    /*
    * 修改分类
    * */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        //对象属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);

        //设置修改时间、修改人
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.update(category);
    }

    /*
    * 根据类型查询分类
    * */
    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}
