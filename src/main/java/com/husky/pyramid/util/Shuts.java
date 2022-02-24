package com.husky.pyramid.util;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 数据分流工具
 * @param <T>
 *
 * @author programmer_husky
 * TODO: 并行替换为 {@link java.util.concurrent.ForkJoinPool} 实现
 */
@Data
public class Shuts<T> {
	List<Shuts.Shut<T>> list = new ArrayList<>();
	/**
	 * 切片容量
	 */
	int shutSize;
	/**
	 * 切片数量
	 */
	int size;

	public Shuts(int shutSize, List<T> elements) {
		if (CollectionUtils.isEmpty(elements)) {
			return;
		}
		this.shutSize = (shutSize <= 0 ? 10 : shutSize);
		this.size = (elements.size() - 1)/shutSize + 1;

		int index = 0;
		for (int i = 0; i < size; i++) {
			List<T> shutElements = new ArrayList<>(shutSize);
			int tempIndex = 0;
			while (tempIndex < shutSize && index < elements.size()) {
				shutElements.add(elements.get(index));
				tempIndex++;
				index++;
			}
			list.add(new Shut<>(shutElements));
		}
	}

	/**
	 * 返回分流后的嵌套集合
	 * @return 两层集合，外层为分流层
	 */
	public List<List<T>> toList() {
		List<List<T>> result = new ArrayList<>(size);
		list.forEach(shut -> result.add(shut.getElements()));
		return result;
	}

	/**
	 * 分流执行指定的函数并获得聚合后的返回值
	 * @param function 指定的函数
	 * @param <R> 返回集合类型
	 * @return 分流执行后汇总的结果
	 */
	public <R> List<R> invoke(Function<List<T>, List<R>> function) {
		List<R> resultList = new ArrayList<>(shutSize*size);
		this.list.forEach(shut -> {
			List<R> apply = function.apply(shut.getElements());
			resultList.addAll(apply);
		});
		return resultList;
	}


	/**
	 * 并行执行函数并获得聚合后的返回值
	 *
	 * <p>保证输入输出顺序
	 * 使用forEachOrdered可实现
	 *
	 * @param function 指定的函数
	 * @param <R> 返回集合类型
	 * @return 分流执行后汇总的结果
	 */
	public <R> List<R> parallelInvoke(Function<List<T>, List<R>> function) {
		List<R> resultList = new ArrayList<>(shutSize*size);
		this.list.parallelStream().forEachOrdered(shut -> {
			List<R> apply = function.apply(shut.getElements());
			resultList.addAll(apply);
		});
		return resultList;
	}


	@Data
	public static class Shut<T>{
		List<T> elements;

		public Shut(List<T> elements) {
			this.elements = elements;
		}
	}
}

