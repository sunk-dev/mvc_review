package com.spring.mvc.util.upload;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.UUID;

public class FileUtil {
    /**
     * 사용자가 클라이언트에서 파일을 전송했을때
     * 중복이 없는 새로운 파일을 생성하여 해당 파일명으로 
     * 날짜별 폴더로 업로드하는 메서드
     * @param file- 사용자가 업로드한 파일의 정보객체
     * @param rootPath- 서버에 업로드할 루트 경로
     * @return- 업로드가 완료 되었을 경우 업로드된 파일의 풀경로를 리턴
     *          ex) /2023/12/29/djfljkljlkjl_고양이.jpg
     */
    public static String
    uploadFile(MultipartFile file,String rootPath){
        //원본 파일명을 중복이 없는 랜덤 이름으로 변경
        String originalFilename = file.getOriginalFilename();
        String newFileName= UUID.randomUUID()+"_"+originalFilename;
        
        //이파일을 날짜별로 관리하기 위해 날짜별 폴더를 생성
        String newUploadPath=makeDateFormatDirectory(rootPath);
        //파일 업로드 수행
        try {
            file.transferTo(new File(newUploadPath,newFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 루트 경로를 받아서 일자별로 폴더를 생성한후
     * 루트경로=날짜 폴더 경로를 반환
     * @param rootPath- 파일 업로드 루트 경로
     * @return- 날짜 폴더 경로가 포함된 새로운 업로드 경로
     *          ex) D:/spring-prj/upload/2023/12/29
     */
    private static String makeDateFormatDirectory(String rootPath) {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();

        String[] dateInfo={year+"",len2(month),len2(day)};

        String directoryPath=rootPath;
        for (String s : dateInfo) {
            directoryPath+="/"+s;
            File f = new File(directoryPath);
            if(!f.exists()) f.mkdir();
        }

        return directoryPath;
    }

    /**
     *
     *한글자 월과 한글자 일을 두글자로 변환해주는 메서드
     * 2023 6.7 2023/06/07
     * @param n- 원본일자나 월자
     * @return- 앞에 0이 붙은 일자나 월자
     */
    private static String len2(int n){

        return new DecimalFormat("00").format(n);
    }
}
