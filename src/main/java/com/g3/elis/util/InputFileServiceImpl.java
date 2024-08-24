package com.g3.elis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.g3.elis.config.FileStorageConfig;
import com.g3.elis.dto.form.UserDto;
import com.g3.elis.dto.report.CoursePerformance;
import com.g3.elis.dto.report.CourseProgress;
import com.g3.elis.dto.report.QuizPerformance;
import com.g3.elis.model.InputFile;
import com.g3.elis.repository.InputFileRepository;
import com.g3.elis.service.ReportService;
import com.g3.elis.service.UserService;

@Service
public class InputFileServiceImpl implements InputFileService {

	@Autowired
	private InputFileRepository inputFileRepository;

	@Autowired
	private UserService userService;
	
	@Autowired
	private FileStorageConfig fileStorageConfig;

	@Override
	public String determineFileType(MultipartFile file) {
		String contentType = file.getOriginalFilename();
		if (contentType.contains(".mp4")) {
			return "video";
		} else if (contentType.contains(".png") || contentType.contains(".jpg")) {
			return "image";
		}
		return null;
	}

	@Override
	public void saveFile(MultipartFile file,String filePath) {
		InputFile inputFile = new InputFile();
		inputFile.setFileName(file.getOriginalFilename());
		try {
			fileStorageConfig.saveFile(file, file.getOriginalFilename(),filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		inputFileRepository.save(inputFile);
	}

	@Override
	public List<InputFile> getAllInputFiles() {
		return inputFileRepository.findAll();
	}

	@Override
	public List<InputFile> getAllImageFiles() {
		List<InputFile> imageFiles = new ArrayList<>();
		for (InputFile inputFile : getAllInputFiles()) {
			if (inputFile.getFileName().contains(".jpg") || inputFile.getFileName().contains(".png")) {
				imageFiles.add(inputFile);
			}
		}
		return imageFiles;
	}

	@Override
	public List<InputFile> getAllVideoFiles() {
		List<InputFile> videoFiles = new ArrayList<>();
		for (InputFile inputFile : getAllInputFiles()) {
			if (inputFile.getFileName().contains(".mp4")) {
				videoFiles.add(inputFile);
			}
		}
		return videoFiles;
	}

	@Override
	public List<InputFile> getAllYouTubeUrl() {
		List<InputFile> YouTubeUrl = new ArrayList<>();
		for (InputFile inputFile : getAllInputFiles()) {
			if (!inputFile.getFileName().contains(".")) {
				YouTubeUrl.add(inputFile);
			}
		}
		return YouTubeUrl;
	}

	@Override
	public void saveYouTubeVideoFile(String url) {
		InputFile inputFile = new InputFile();

		String videoId = null;
		// https://www.youtube.com/watch?v=_Dd5RENCKLU
		if (url.contains("youtube.com")) {
			Pattern pattern = Pattern.compile("v=([^&]+)");
			Matcher matcher = pattern.matcher(url);
			if (matcher.find()) {
				videoId = matcher.group(1);
			}
		} else
			return;

		inputFile.setFileName(videoId);
		inputFileRepository.save(inputFile);
	}

	@Override
	public List<InputFile> getAllExcelFiles() {
		List<InputFile> ExcelFiles = new ArrayList<>();
		for (InputFile inputFile : getAllInputFiles()) {
			if (inputFile.getFileName().contains(".xlsx") || inputFile.getFileName().contains(".xlsm")) {
				ExcelFiles.add(inputFile);
			}
		}
		return ExcelFiles;
	}

	@Override
	public List<SheetData> readExcel(InputFile file) throws IOException {
		List<SheetData> allSheetsData = new ArrayList<>();

		File readFile = new File("./src/main/resources/static/upload-resources/" + file.getFileName());
		FileInputStream readFileInputStream = new FileInputStream(readFile);
		Workbook workbook = new XSSFWorkbook(readFileInputStream);
		FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

		for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			List<List<String>> rows = new ArrayList<>();

			for (Row row : sheet) {
				List<String> cells = new ArrayList<>();

				for (Cell cell : row) {
					switch (formulaEvaluator.evaluateInCell(cell).getCellType()) {
					case STRING:
						cells.add(cell.getStringCellValue());
						break;
					case NUMERIC:
						if (DateUtil.isCellDateFormatted(cell)) {
							cells.add(new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue()));
						} else {
							double numericValue = cell.getNumericCellValue();
							if (numericValue == (long) numericValue) {
								cells.add(String.valueOf((long) numericValue));
							} else {
								cells.add(String.valueOf(cell.getNumericCellValue()));
							}
						}
						break;
					case BOOLEAN:
						cells.add(String.valueOf(cell.getBooleanCellValue()));
						break;
					case FORMULA:
						CellValue cellValue = formulaEvaluator.evaluate(cell);
						switch (cellValue.getCellType()) {
						case STRING:
							cells.add(cellValue.getStringValue());
							break;
						case NUMERIC:
							cells.add(String.valueOf(cellValue.getNumberValue()));
							break;
						case BOOLEAN:
							cells.add(String.valueOf(cellValue.getBooleanValue()));
							break;
						default:
							cells.add(" ");
							break;

						}
						break;
					case BLANK:
						cells.add("");
						break;
					default:
						cells.add("");
						break;
					}
				}
				rows.add(cells);
			}
			allSheetsData.add(new SheetData(sheet.getSheetName(), rows));
		}
		workbook.close();
		return allSheetsData;
	}

	@Override
	public void WriteEmployeeDataFromExcel(MultipartFile excelFile) throws IOException 
	{
		Workbook workbook = new XSSFWorkbook(excelFile.getInputStream());
		FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
		for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) 
		{
			boolean startCollectingData = false;
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			if (sheet.getSheetName().contains("Employee_Data") && sheet.getSheetName().equalsIgnoreCase("Employee_Data")) 
			{
				for (Row row : sheet) 
				{	
					UserDto userDto = new UserDto();
					for (Cell cell : row) 
					{
						if(getCellValue(formulaEvaluator,cell).contains("Sr.") || startCollectingData == true)
						{
							if(cell==null || getCellValue(formulaEvaluator,cell)=="") break;
							if(startCollectingData == false)
							{
								startCollectingData = true;
								break;
							}
							if(getCellValue(formulaEvaluator,cell).contains("Division"))
							{
								userDto.setDivision(getCellValue(formulaEvaluator,cell));
							}
							if(getCellValue(formulaEvaluator,cell).contains("25-") || getCellValue(formulaEvaluator,cell).contains("26-"))
							{
								if (getCellValue(formulaEvaluator,cell).contains("25-"))
								{
									userDto.setGender("Male");
								}
								else if(getCellValue(formulaEvaluator,cell).contains("26-"))
								{
									userDto.setGender("Female");
								}
								else
								{
									userDto.setGender("Other");
								}
								userDto.setStaffId(getCellValue(formulaEvaluator,cell));
							}
							if(getCellValue(formulaEvaluator,cell).contains("Dept"))
							{
								userDto.setDept(getCellValue(formulaEvaluator,cell));
							}
							if(getCellValue(formulaEvaluator,cell).contains("@diracetechnology.com"))
							{
								userDto.setEmail(getCellValue(formulaEvaluator,cell));
							}
							
							if(getCellValue(formulaEvaluator,row.getCell(3))!=null)
							{
								userDto.setName(getCellValue(formulaEvaluator,row.getCell(3)));
							}
							if(getCellValue(formulaEvaluator,row.getCell(4))!=null)
							{
								userDto.setDoorLogNo(getCellValue(formulaEvaluator,row.getCell(4)));
							}
							if(getCellValue(formulaEvaluator,row.getCell(6))!=null)
							{
								userDto.setTeam(getCellValue(formulaEvaluator,row.getCell(6)));
							}
							if(getCellValue(formulaEvaluator,row.getCell(8))!=null)
							{
								userDto.setStatus(getCellValue(formulaEvaluator,row.getCell(8)));
							}
							if(getCellValue(formulaEvaluator,row.getCell(9))!=null)
							{
								userDto.setRole(getCellValue(formulaEvaluator,row.getCell(9)));
							}
						}
					}
					if(userDto.getName()!=null)
					{
						userService.createUser(userDto);
					}
				}
			}	
		}
		workbook.close();
	}
	
	private String getCellValue(FormulaEvaluator formulaEvaluator,Cell cell)
	{
		if(cell!=null)
		{
			switch (formulaEvaluator.evaluateInCell(cell).getCellType()) {
			case STRING:
				return cell.getStringCellValue();
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return (new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue()));
				} else {
					double numericValue = cell.getNumericCellValue();
					if (numericValue == (long) numericValue) {
						return String.valueOf((long) numericValue);
					} else {
						return String.valueOf(cell.getNumericCellValue());
					}
				}
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case FORMULA:
				CellValue cellValue = formulaEvaluator.evaluate(cell);
				switch (cellValue.getCellType()) {
				case STRING:
					return cellValue.getStringValue();
				case NUMERIC:
					return String.valueOf(cellValue.getNumberValue());
				case BOOLEAN:
					return String.valueOf(cellValue.getBooleanValue());
				default:
					return "";
				}
			case BLANK:
				return "";
			default:
				return "";
			}
		}
		else
		{
			return "";
		}
	}

	@Override
	public void generateCoursePerformanceExcelReportFile(List<CoursePerformance> reportData)
	{
		Workbook workbook = new XSSFWorkbook();
		
		String sheetName = "Course Performance Report";
		createSheetsWithCoursePerformanceData(workbook, sheetName,reportData);
		// Write the workbook to a file
        try (FileOutputStream fileOut = new FileOutputStream(("Course_Performance_Report_"+Timestamp.valueOf(LocalDateTime.now()).toString().substring(0,10))+".xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the workbook
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	private void createSheetsWithCoursePerformanceData(Workbook workbook,String sheetName,List<CoursePerformance> reportData)
	{
		Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        
		String[] headers = {"Course Title","Course Id","Instructor","Created At","Enrollment","Monthly Enrolled User"
							,"Yearly Enrolled User","Completion Rate","Average Score"};
		CellStyle headerStyle = createHeaderCellStyle(workbook);
		for (int i = 0; i < headers.length; i++) 
			{
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
		
		CellStyle dataStyle = createDataCellStyle(workbook);
		for (int i = 0; i < reportData.size(); i++) 
		{
            Row row = sheet.createRow(i + 1);
            List<String> dataList = new ArrayList<String>();
    		dataList.add(reportData.get(i).getCourseTitle().toString());
    		dataList.add(String.valueOf(reportData.get(i).getCourseId()));
    		dataList.add(reportData.get(i).getInstructor().toString());
    		dataList.add(reportData.get(i).getCreatedAt().toString());
    		dataList.add(String.valueOf(reportData.get(i).getEnrollment()));
    		dataList.add(String.valueOf(reportData.get(i).getMonthlyEnrolledUser()));
    		dataList.add(String.valueOf(reportData.get(i).getYearlyEnrolledUser()));
    		dataList.add(String.valueOf(reportData.get(i).getCompletionRate()));
    		dataList.add(String.valueOf(reportData.get(i).getAverageScore()));
    		for(int j = 0; j < dataList.size();j++)
    		{
    			Cell cell = row.createCell(j);
                cell.setCellValue(dataList.get(j));
                cell.setCellStyle(dataStyle);
    		}
        }
	}
	
	@Override
	public void generateCourseProgressExcelReportFile(List<CourseProgress> reportData) {
		Workbook workbook = new XSSFWorkbook();
		
		String sheetName = "Course Performance Report";
		createSheetsWithCourseProgressData(workbook, sheetName,reportData);
		// Write the workbook to a file
        try (FileOutputStream fileOut = new FileOutputStream(("Course_Progress_Report_"+Timestamp.valueOf(LocalDateTime.now()).toString().substring(0,10))+".xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the workbook
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	private void createSheetsWithCourseProgressData(Workbook workbook,String sheetName,List<CourseProgress> reportData)
	{
		Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        
		String[] headers = {"Course Title","Course Id","Enrolled User Name","Staff Id","Enrolled Date","Progress"
							,"Average Score","Grade"};
		
		CellStyle headerStyle = createHeaderCellStyle(workbook);
		for (int i = 0; i < headers.length; i++) 
			{
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
		CellStyle dataStyle = createDataCellStyle(workbook);
		for (int i = 0; i < reportData.size(); i++) 
		{
			Row row = sheet.createRow(i + 1);
            List<String> dataList = new ArrayList<String>();
            dataList.add(reportData.get(i).getCourseTitle());
            dataList.add(String.valueOf(reportData.get(i).getCourseId()));
            dataList.add(reportData.get(i).getStaffId());
            dataList.add(reportData.get(i).getEnrolledUserName());
            dataList.add(reportData.get(i).getEnrolledDate().toString());
            dataList.add(String.valueOf(reportData.get(i).getProgress()));
            dataList.add(String.valueOf(reportData.get(i).getAverageScore()));
            dataList.add(reportData.get(i).getGrade());
            for(int j = 0; j < dataList.size();j++)
    		{
    			Cell cell = row.createCell(j);
                cell.setCellValue(dataList.get(j));
                cell.setCellStyle(dataStyle);
    		}
		}
	}

	@Override
	public void generateQuizPerformanceExcelReportFile(List<QuizPerformance> reportData) {
		Workbook workbook = new XSSFWorkbook();
		
		String sheetName = "Course Performance Report";
		createSheetsWithQuizPerformanceData(workbook, sheetName,reportData);
		// Write the workbook to a file
        try (FileOutputStream fileOut = new FileOutputStream(("Quiz_Performance_Report_"+Timestamp.valueOf(LocalDateTime.now()).toString().substring(0,10))+".xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the workbook
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }		
	}
		
	private void createSheetsWithQuizPerformanceData(Workbook workbook,String sheetName,List<QuizPerformance> reportData)
	{

		Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        
		String[] headers = {"Student","Course","Assignment Title","Average Score","Passed Rate","Highest Score","Lowest Score"};
		
		CellStyle headerStyle = createHeaderCellStyle(workbook);
		for (int i = 0; i < headers.length; i++) 
			{
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
		CellStyle dataStyle = createDataCellStyle(workbook);
		for (int i = 0; i < reportData.size(); i++) 
		{
			Row row = sheet.createRow(i + 1);
            List<String> dataList = new ArrayList<String>();
            dataList.add(reportData.get(i).getStudentName());
            dataList.add(reportData.get(i).getCourseTitle());
            dataList.add(reportData.get(i).getAssignmentTitle());
            dataList.add(String.valueOf(reportData.get(i).getAverageScore()));
            dataList.add(String.valueOf(reportData.get(i).getPassRate()));
            dataList.add(String.valueOf(reportData.get(i).getHighestScore()));
            dataList.add(String.valueOf(reportData.get(i).getLowestScore()));
            
            for(int j = 0; j < dataList.size();j++)
    		{
    			Cell cell = row.createCell(j);
                cell.setCellValue(dataList.get(j));
                cell.setCellStyle(dataStyle);
    		}
		}
	}
	
	
	private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
	
	private static CellStyle createDataCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}

