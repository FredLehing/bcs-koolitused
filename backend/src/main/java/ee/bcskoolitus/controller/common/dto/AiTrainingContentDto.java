package ee.bcskoolitus.controller.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiTrainingContentDto implements Serializable {

    @Schema(description = "Koolituse AI-ga koostatud pealkiri",
            example = "PDF-ist genereeritud pealkiri (TO BE IMPLEMENTED)")
    private String title;

    @Schema(description = "Koolituse AI-ga koostatud lühikirjeldus",
            example = "PDF-ist genereeritud lühikirjeldus (TO BE IMPLEMENTED)")
    private String shortDescription;

    @Schema(description = "Koolituse AI-ga koostatud kirjeldus",
            example = "PDF-ist genereeritud kirjeldus (TO BE IMPLEMENTED)")
    private String description;
}
