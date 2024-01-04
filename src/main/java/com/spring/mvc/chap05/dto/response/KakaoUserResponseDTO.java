package com.spring.mvc.chap05.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString

public class KakaoUserResponseDTO {
    private  long id;
    @JsonProperty("connected_at")
    private LocalDateTime connectedAt;
    private Properties properties;

    @Setter @Getter @ToString
    public static  class Properties{
        private String nickname;
        @JsonProperty("profile_image")
        private String profileImage;
        @JsonProperty("thumbnail_image")
        private String thumbnailImage;


    }

}
