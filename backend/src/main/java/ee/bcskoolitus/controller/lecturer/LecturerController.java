package ee.bcskoolitus.controller.lecturer;

import ee.bcskoolitus.controller.lecturer.dto.LecturerDto;
import ee.bcskoolitus.service.LecturerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LecturerController {
    private final LecturerService lecturerService;

    @GetMapping("/lecturers")
    @Operation(summary = "Otsib lektoreid nime järgi",
            description = "Tagastab lektorid, kelle nimi sisaldab otsingusõna (tõstutundetu), nime järgi tähestikuliselt. Tühi otsingusõna tagastab kõik lektorid. Kasutatakse TrainingFormView \"Vali lektor\" modalis.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<LecturerDto> findLecturers(@RequestParam(required = false, defaultValue = "") String search) {
        return lecturerService.findLecturers(search);
    }
}
