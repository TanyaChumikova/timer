package com.igorgorbunov3333.timer.service.console.command.impl;

import com.igorgorbunov3333.timer.model.dto.PeriodDto;
import com.igorgorbunov3333.timer.model.dto.pomodoro.PomodoroDto;
import com.igorgorbunov3333.timer.service.console.command.CommandProcessor;
import com.igorgorbunov3333.timer.service.console.command.CurrentCommandStorage;
import com.igorgorbunov3333.timer.service.console.command.line.session.TagPomodoroSessionMapper;
import com.igorgorbunov3333.timer.service.console.printer.PrinterService;
import com.igorgorbunov3333.timer.service.console.printer.StandardReportPrinter;
import com.igorgorbunov3333.timer.service.console.printer.util.SimplePrinter;
import com.igorgorbunov3333.timer.service.exception.FreeSlotException;
import com.igorgorbunov3333.timer.service.pomodoro.provider.impl.CurrentDayPomodoroProvider;
import com.igorgorbunov3333.timer.service.pomodoro.saver.PomodoroAutoSaver;
import com.igorgorbunov3333.timer.service.util.CurrentTimeService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalTime;
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
    public void process() {
        Integer numberOfPomodoroToSave = getNumberFromCommand();

        if (numberOfPomodoroToSave == null) {
            return;
        }

        List<PomodoroDto> savedPomodoroList = save(numberOfPomodoroToSave);

        if (CollectionUtils.isEmpty(savedPomodoroList)) {
            return;
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
            SimplePrinter.print(String.format("Incorrect number [%s]", pomodoroNumber));
            return null;
        }
    }

    private List<PomodoroDto> save(int numberToSave) {
        List<PomodoroDto> savedPomodoro;
        try {
            savedPomodoro = pomodoroAutoSaver.save(numberToSave);

            savedPomodoro.forEach(this::printSuccessfullySavedMessage);

            return savedPomodoro;
        } catch (FreeSlotException e) {
            String errorMessage = e.getMessage();
            SimplePrinter.print(errorMessage);
            return null;
        }
    }

}
