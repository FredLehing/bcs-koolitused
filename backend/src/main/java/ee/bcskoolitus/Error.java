package ee.bcskoolitus;

import lombok.Getter;

@Getter
public enum Error {
    INCORRECT_CREDENTIALS("Vale email või parool"),
    TRANSLATION_EXISTS("Selles keeles tõlge on juba olemas"),
    TRAINING_DELETED("Kustutatud koolituse staatust ei saa muuta, taasta see enne"),
    PHOTO_TYPE_NOT_ALLOWED("Lubatud on ainult PNG, JPEG või WebP pilt"),
    PHOTO_TOO_LARGE("Pilt on liiga suur, lubatud kuni 2 MB"),
    LECTURER_HAS_UPCOMING_COURSES("Koolitajal on tulevasi toimumiskordi, vali neile enne teine koolitaja"),

    COURSE_END_BEFORE_START("Lõppkuupäev ei saa olla varasem kui alguskuupäev"),
    ROOM_HAS_UPCOMING_COURSES("Ruumis on tulevasi toimumiskordi, vali neile enne teine ruum"),
    ROOM_NAME_EXISTS("Sellise nimega ruum on juba olemas"),

    EMAIL_TAKEN("Selle e-posti aadressiga konto on juba olemas"),
    COURSE_FULL("Toimumiskord on täis"),
    ALREADY_REGISTERED("Oled sellele toimumiskorrale juba registreerunud"),
    REGISTRATION_CLOSED("Registreerumine on lõppenud");

    private final String message;

    Error(String message) {
        this.message = message;
    }
}
