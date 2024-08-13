package com.g3.elis.dto.dtoServiceImpl;

import org.springframework.stereotype.Service;

import com.g3.elis.dto.dtoService.CourseMaterialDtoService;
import com.g3.elis.dto.form.CourseMaterialDto;

@Service
public class CourseMaterialDtoServiceImpl implements CourseMaterialDtoService{

	@Override
	public CourseMaterialDto createMaterialDto(int index, String title,String content) {
		CourseMaterialDto courseMaterialDto = new CourseMaterialDto();
		courseMaterialDto.setContent(content);
		courseMaterialDto.setIndex(index);
		courseMaterialDto.setTitle(title);
		return courseMaterialDto;
	}



}