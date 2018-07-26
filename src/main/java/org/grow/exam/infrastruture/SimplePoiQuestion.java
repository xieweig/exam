package org.grow.exam.infrastruture;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.grow.exam.domain.Question;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:
 */
@Service
public class SimplePoiQuestion implements PoiQuestion, InitializingBean {


    @Value("${poi.readfrom:/static/test_content.xlsx}")
    private String url;
    @Value("${poi.sheetNumber:0}")
    private Integer sheetNumber;


    private Workbook workbook = null;

    @Override
    public void close() throws IOException {
        this.workbook.close();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.notNull(url,"please tell me which file to read by poi");
        Assert.notNull(workbook, "load failed about workbook");
    }

    @PostConstruct
    public void init(){
        try {
            workbook = WorkbookFactory.create(new ClassPathResource(url).getFile());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Question> selectAll(InputStream inputStream, int startRow, int size) {
        try {
            workbook = WorkbookFactory.create(inputStream);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
        return this.selectAll(startRow, size);
    }

    @Override
    public List<Question> selectAll(int startRow, int size) {

        List<Question> questions = new LinkedList<Question>();

        Sheet sheet = workbook.getSheetAt(sheetNumber);

        for (int i = 0; i < size ; i++) {

            Row row = sheet.getRow(startRow+i);
            questions.add(this.map(row));

        }

        return questions;
    }

    public Question map(Row row){
        Question question = new Question();
        question.setQuestionCode(this.getValue(row.getCell(0)));
        question.setTitle(this.getValue(row.getCell(1)));
        question.setOptionA(this.getValue(row.getCell(2)));
        question.setOptionB(this.getValue(row.getCell(3)));
        question.setOptionC(this.getValue(row.getCell(4)));
        question.setOptionD(this.getValue(row.getCell(5)));
        return question;
    }

    public String getValue(Cell cell) {
        if(cell==null){
            return "---";
        }
        if (cell.getCellTypeEnum()== CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
            double cur=cell.getNumericCellValue();
            long longVal = Math.round(cur);
            Object inputValue = null;
            if(Double.parseDouble(longVal + ".0") == cur)
                inputValue = longVal;
            else
                inputValue = cur;
            return String.valueOf(inputValue);
        } else if(cell.getCellTypeEnum() == CellType.BLANK || cell.getCellTypeEnum() == CellType.ERROR){
            return "---";
        }
        else {
            return String.valueOf(cell.getStringCellValue());
        }
    }

    @Override
    public Map<String, Integer> readStandardAnswer(InputStream inputStream,int startRow, int size) {
        try {
            workbook = WorkbookFactory.create(inputStream);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
        Map<String, Integer> correctAnswers = new HashMap<>();

        Sheet sheet = workbook.getSheetAt(sheetNumber);

        for (int i = 0; i < size ; i++) {

            Row row = sheet.getRow(startRow+i);
            /**
            **
            * xieweig notes: 第0列是code编号第7列也就是H列是映射后的正确答案
            */
            correctAnswers.put(this.getValue(row.getCell(0)),
                    (int)row.getCell(7).getNumericCellValue());
        }

        return correctAnswers;
    }



    @PreDestroy
    public void destroy(){
        System.out.println("workbook is null? " +workbook+" to destroy");
    }

}
