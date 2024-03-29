package com.igorgorbunov3333.timer.service.console.command.impl;

import com.igorgorbunov3333.timer.model.dto.pomodoro.PomodoroDto;
import com.igorgorbunov3333.timer.service.console.command.CommandProcessor;
import com.igorgorbunov3333.timer.service.console.command.line.provider.CommandProvider;
import com.igorgorbunov3333.timer.service.console.command.line.session.NumberProvidable;
import com.igorgorbunov3333.timer.service.console.command.line.session.coordinator.MonthSessionCommandCoordinator;
import com.igorgorbunov3333.timer.service.console.printer.util.SimplePrinter;
import com.igorgorbunov3333.timer.service.pomodoro.provider.impl.CurrentYearPomodoroProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class MonthCommandProcessor implements CommandProcessor, NumberProvidable {

    private final CurrentYearPomodoroProvider currentYearPomodoroProvider;
    @Getter
    private final CommandProvider commandProvider;
    private final MonthSessionCommandCoordinator monthSessionCommandCoordinator;

    @Override
    @Transactional(readOnly = true)
    public void process() {
        List<PomodoroDto> yearlyPomodoro = currentYearPomodoroProvider.provide(null);

        while (true) {
            SimplePrinter.printParagraph();
            SimplePrinter.print("Choose an option how to display monthly pomodoro or \"e\" to exit");
            SimplePrinter.print("1. Current month");
            SimplePrinter.print("2. Previous month");

            int numberAnswer = provideNumber();

            if (numberAnswer == -1) {
                break;
            }

            if (numberAnswer > 2) {
                SimplePrinter.print("Wrong number. Try again");
            } else {
                monthSessionCommandCoordinator.coordinate(String.valueOf(numberAnswer), yearlyPomodoro);
            }
        }

        SimplePrinter.print("Monthly pomodoro menu abandoned");
    }

    @Override
    public String command() {
        return "6";
    }

}
