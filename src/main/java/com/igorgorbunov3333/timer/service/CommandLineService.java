package com.igorgorbunov3333.timer.service;

import com.igorgorbunov3333.timer.model.entity.Pomodoro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Component
public class CommandLineService {

    @Autowired
    private PomodoroService pomodoroService;
    @Autowired
    private SecondsFormatterService secondsFormatterService;

    public void start() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Pomodoro application started");
        System.out.println();
        printFeaturesList();
        while (true) {
            String text = sc.nextLine();
            gotoChoice(text);
            if (text.equals("exit"))
                break;
        }
        sc.close();
    }

    private void gotoChoice(String input) {
        if (input.equals("1")) {
            pomodoroService.starPomodoro();
        } else if (input.equals("2")) {
            pomodoroService.stopPomodoro();
        } else if (input.equals("3")) {
            int seconds = pomodoroService.getPomodoroTime();
            String formattedTime = secondsFormatterService.format(seconds);
            System.out.println(formattedTime);
        } else if (input.equals("4")) {
            System.out.println(pomodoroService.getPomodorosInDay());
        } else if (input.equals("5")) {
            List<Pomodoro> pomodoros = pomodoroService.getPomodorosInDayExtended();
            for (Pomodoro pomodoro : pomodoros) {
                printPomodoro(pomodoro, true);
            }
        } else if (input.equals("6")) {
            Map<LocalDate, List<Pomodoro>> datesToPomadoros = pomodoroService.getPomodorosInMonthExtended();
            for (Map.Entry<LocalDate, List<Pomodoro>> entry : datesToPomadoros.entrySet()) {
                System.out.println();
                System.out.println(entry.getKey());
                System.out.println("pomodoros in day - " + entry.getValue().size());
                for (Pomodoro pomodoro : entry.getValue()) {
                    printPomodoro(pomodoro, false);
                }
            }
        } else if (input.equals("help")) {
            printFeaturesList();
        } else if (input.startsWith("remove")) {
            char[] inputChars = input.toCharArray();
            int index = "remove ".length();
            char[] pomodoroIdInString = new char[input.length() - index];
            for (int i = index, j = 0; i < inputChars.length; i++, j++) {
                pomodoroIdInString[j] = inputChars[i];
            }
            Long pomodoroId = Long.valueOf(new String(pomodoroIdInString));
            pomodoroService.removePomodoro(pomodoroId);
        }
    }

    private void printFeaturesList() {
        System.out.println("1. start");
        System.out.println("2. stop");
        System.out.println("3. current time");
        System.out.println("4. pomadoros today");
        System.out.println("5. pomadoros today extended");
        System.out.println("6. pomadoros for the last month");
        System.out.println("7. remove pomodoro by id. For example \"remove 10\"");
    }

    private void printPomodoro(Pomodoro pomodoro, boolean withId) {
        String pomodoroPeriod = mapTimestamp(pomodoro);
        String pomodoroDuration = secondsFormatterService.format(pomodoro.startEndTimeDifferenceInSeconds());
        String formattedPomodoroPeriodAndDuration = "time - "
                .concat(pomodoroPeriod)
                .concat(" | ")
                .concat("duration - ")
                .concat(pomodoroDuration);
        String pomodoroRow;
        if (withId) {
            String pomodoroId = pomodoro.getId().toString();
            pomodoroRow = "id - ".concat(pomodoroId)
                    .concat(" | ")
                    .concat(formattedPomodoroPeriodAndDuration);
        } else {
            pomodoroRow = formattedPomodoroPeriodAndDuration;
        }
        System.out.println(pomodoroRow);
    }

    private String mapTimestamp(Pomodoro pomodoro) {
        String startTimeString = format(pomodoro.getStartTime());
        String endTimeString = format(pomodoro.getEndTime());
        return String.join(" : ", List.of(startTimeString, endTimeString));
    }

    private String format(LocalDateTime startTime) {
        int hours = startTime.getHour();
        int minutes = startTime.getMinute();
        int seconds = startTime.getSecond();
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

}
