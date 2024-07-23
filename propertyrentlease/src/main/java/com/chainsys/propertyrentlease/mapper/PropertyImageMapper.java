package com.chainsys.propertyrentlease.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import org.springframework.jdbc.core.RowMapper;

import com.chainsys.propertyrentlease.model.PropertyImage;

public class PropertyImageMapper implements RowMapper<PropertyImage> {

	@Override
	public PropertyImage mapRow(ResultSet rs, int rowNum) throws SQLException {
		PropertyImage propertyimage = new PropertyImage();
		propertyimage.setImageId(rs.getInt("image_id"));
		propertyimage.setImages(Base64.getEncoder().encodeToString(rs.getBytes("images")));
		propertyimage.setPropertyId(rs.getInt("property_id"));

		return propertyimage;
	}

}
