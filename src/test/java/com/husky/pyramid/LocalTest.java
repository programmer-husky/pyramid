package com.husky.pyramid;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LocalTest {

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<>(99);
		for (int i = 0; i < 10; i++) {
			list.add(i);
		}
		List<Integer> result = new ArrayList<>();
		list.parallelStream().forEach(result::add);
		result.forEach(System.out::println);
	}

	private static List<String> convert(List<Integer> list) {
		return list.stream().map(item -> "haha-" + item).collect(Collectors.toList());
	}
}
