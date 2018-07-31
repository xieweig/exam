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
import sun.reflect.generics.tree.Tree;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:
 */
@Service
public class SimplePoiQuestion implements PoiQuestion, InitializingBean {


/*    @Value("${poi.readfrom:/static/test_content.xlsx}")
    private String url;*/
    @Value("${poi.sheetNumber:0}")
    private Integer sheetNumber;


    private Workbook workbook = null;

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

       // Assert.notNull(url,"please tell me which file to read by poi");
        //Assert.notNull(workbook, "please set workBook just like set SessionFactory before use hibernate CRUD load failed about workbook");
    }
/*
    @PostConstruct
    public void init(){
        try {
            workbook = WorkbookFactory.create(new ClassPathResource(url).getFile());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
    }*/

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

    private Question map(Row row){
        Question question = new Question();
        /**
        **
        * xieweig notes: 对于序号做一个四位的补零字符串化，方便排序，直接用数字应该也可以。
        */
        question.setQuestionCode(String.format("%04d",(int)row.getCell(0).getNumericCellValue()));
        question.setTitle(this.getValue(row.getCell(1)));
        question.setOptionA(this.getValue(row.getCell(2)));
        question.setOptionB(this.getValue(row.getCell(3)));
        question.setOptionC(this.getValue(row.getCell(4)));
        question.setOptionD(this.getValue(row.getCell(5)));
        return question;
    }
    /**
    **
    * xieweig notes: 将所有的值都读成字符串格式，copy来的
    */
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
    public Map<String, Integer> readStandardAnswer(int startRow, int size) {
        /**
        **
        * xieweig notes: treemap自带排序功能 也可以传入参数传一个lambda表达式，都是根据key值的
        */
        //Map<String, Integer> correctAnswers = new TreeMap<>();
        Map<String, Integer> correctAnswers = new TreeMap<>((str1, str2)-> str2.compareTo(str1));

        Sheet sheet = workbook.getSheetAt(sheetNumber);

        for (int i = 0; i < size ; i++) {

            Row row = sheet.getRow(startRow+i);
            /**
            **
            * xieweig notes: 第0列是code编号第7列也就是H列是映射后的正确答案
            */
            correctAnswers.put(String.format("%04d",(int)row.getCell(0).getNumericCellValue()),
                    (int)row.getCell(7).getNumericCellValue());
        }


        return correctAnswers;
    }



    @PreDestroy
    public void destroy(){
        System.out.println("workbook is null? " +workbook+" to destroy");
    }

}
