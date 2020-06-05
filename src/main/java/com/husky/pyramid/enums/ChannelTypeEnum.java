package com.husky.pyramid.enums;

import lombok.Getter;

/**
 * redis通道类型枚举
 * @author dengweichang
 */
public enum  ChannelTypeEnum {

	/**
	 * 删除缓存
	 */
	DELETE(1),

	/**
	 * 清空缓存
	 */
	CLEAR(2);

	@Getter
	private Integer type;

	ChannelTypeEnum(Integer type) {
		this.type = type;
	}
}
