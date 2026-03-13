package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;

    /*
    * 新增菜品和对应的口味数据
    * @param dishDTO
    * */
    @Transactional // 开启事务
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();

        // 将菜品数据拷贝到dish对象中
        BeanUtils.copyProperties(dishDTO,dish);

        //向菜品表插入1条数据 只需插入菜品数据
        dishMapper.insert(dish);

        //获取Insert语句生成的主键值
        Long dishId = dish.getId();

        //向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });

            // 批量插入口味数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /*
     * 批量删除菜品
     * @param ids
     * */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除 --是否存在起售中的菜品
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                //当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断当前菜品是否能够删除 --菜品是否关联了套餐
        List<Long> setmealIds = setMealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds != null && setmealIds.size() > 0){
            //当前菜品关联了套餐，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品表中的数据  每次循环都要进行SQL删除，效率会比较低
        /*for (Long id : ids) {
            dishMapper.deleteById(id);
        }*/

        //批量删除菜品数据
        //sql: delete from dish where id in (?,?,?)
        dishMapper.deleteByIds(ids);

        //删除菜品关联的口味数据  每次循环都要进行SQL删除，效率会比较低
        /*for (Long id : ids) {
            dishFlavorMapper.deleteByDishId(id);
        }*/

        //批量删除菜品关联的口味数据
        //sql: delete from dish_favor where dish_id in (?,?,?)
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /*
     * 根据id查询菜品和对应的口味数据
     * @param id
     * */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据ID查询菜品数据
        Dish dish = dishMapper.getById(id);

        //根据菜品ID查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        //组装DishVO对象并返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /*
     * 根据ID修改菜品基本信息和对应的口味信息
     * @param dishDTO
     * @return
     * */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        //修改菜品表的基本信息
        dishMapper.update(dish);

        //先根据菜品ID删除口味表中对应的数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //再插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //批量插入口味数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /*
     * 菜品起售、停售
     * @param status
     * @param id
     * */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();

        dishMapper.update(dish);
    }

    /*
     * 根据分类ID查询菜品选项
     * @param categoryId
     * @return
     * */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .status(StatusConstant.ENABLE)
                .categoryId(categoryId)
                .build();
        return dishMapper.list(dish);
    }
}
