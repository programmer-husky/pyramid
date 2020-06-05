package com.husky.pyramid.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class B {

	String name;

	List<String> list;

	public static void main(String[] args) {
		int i = 10000;
		for (int i1 = 0; i1 < 250; i1++) {
			i *= 1.05;
		}
		System.out.println(i);
	}
}
