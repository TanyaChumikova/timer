package com.igorgorbunov3333.timer.service.console.command.impl;

import com.igorgorbunov3333.timer.model.dto.PeriodDto;
import com.igorgorbunov3333.timer.model.dto.pomodoro.PomodoroDto;
import com.igorgorbunov3333.timer.service.console.command.CommandProcessor;
import com.igorgorbunov3333.timer.service.console.command.CurrentCommandStorage;
import com.igorgorbunov3333.timer.service.console.command.line.session.TagPomodoroSessionMapper;
import com.igorgorbunov3333.timer.service.console.printer.PrinterService;
import com.igorgorbunov3333.timer.service.console.printer.StandardReportPrinter;
import com.igorgorbunov3333.timer.service.exception.PomodoroException;
import com.igorgorbunov3333.timer.service.pomodoro.provider.impl.CurrentDayPomodoroProvider;
import com.igorgorbunov3333.timer.service.pomodoro.saver.PomodoroAutoSaver;
import com.igorgorbunov3333.timer.service.util.CurrentTimeService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AutoSaveCommandProcessor extends AbstractPomodoroSessionMapper implements CommandProcessor {

    private final PomodoroAutoSaver pomodoroAutoSaver;
    @Getter
    private final CurrentDayPomodoroProvider currentDayLocalPomodoroProvider;
    @Getter
    private final PrinterService printerService;
    @Getter
    private final TagPomodoroSessionMapper tagPomodoroSessionMapper;
    private final StandardReportPrinter standardReportPrinter;
    private final CurrentTimeService currentTimeService;

    @Override
    @Transactional
    public void process() {
        Integer numberOfPomodoroToSave = getNumberFromCommand();

        if (numberOfPomodoroToSave == null) {
            return;
        }

        List<PomodoroDto> savedPomodoroList = new ArrayList<>();
        for (int i = 0; i < numberOfPomodoroToSave; i++) {
            PomodoroDto singlePomodoro = save();

            if (singlePomodoro == null) {
                return;
            }

            savedPomodoroList.add(singlePomodoro);
        }

        List<Long> pomodoroIdList = savedPomodoroList.stream()
                .map(PomodoroDto::getId)
                .collect(Collectors.toList());
        List<PomodoroDto> dailyPomodoro = startTagSessionAndPrintDailyPomodoro(pomodoroIdList);

        LocalDate currentDay = currentTimeService.getCurrentDateTime().toLocalDate();
        PeriodDto period = new PeriodDto(currentDay.atStartOfDay(), currentDay.atTime(LocalTime.MAX));

        standardReportPrinter.print(period, dailyPomodoro);
    }

    @Override
    public String command() {
        return "save";
    }

    private Integer getNumberFromCommand() {
        String[] autoSaveCommandParts = CurrentCommandStorage.currentCommand.split(StringUtils.SPACE);

        if (autoSaveCommandParts.length > 1) {
            return parseNumber(autoSaveCommandParts[1]);
        } else {
            return 1;
        }
    }

    private Integer parseNumber(String commandNumberPart) {
        Integer pomodoroNumber = null;
        try {
            pomodoroNumber = Integer.valueOf(commandNumberPart);
            return pomodoroNumber;
        } catch (NumberFormatException e) {
            printerService.print(String.format("Incorrect number [%s]", pomodoroNumber));
            return null;
        }
    }

    private PomodoroDto save() {
        PomodoroDto savedPomodoro;
        try {
            savedPomodoro = pomodoroAutoSaver.save();
            printSuccessfullySavedMessage(savedPomodoro);

            return savedPomodoro;
        } catch (PomodoroException e) {
            String errorMessage = e.getMessage();
            printerService.print(errorMessage);
            return null;
        }
    }

}
