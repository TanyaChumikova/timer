package com.igorgorbunov3333.timer.service.pomodoro.provider;

import com.igorgorbunov3333.timer.model.dto.pomodoro.PomodoroDto;
import com.igorgorbunov3333.timer.model.entity.pomodoro.Pomodoro;
import com.igorgorbunov3333.timer.model.entity.pomodoro.PomodoroTag;
import com.igorgorbunov3333.timer.repository.PomodoroRepository;
import com.igorgorbunov3333.timer.service.mapper.PomodoroMapper;
import com.igorgorbunov3333.timer.service.pomodoro.time.calculator.enums.PomodoroPeriod;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//TODO: refactor
public interface LocalPomodoroProvider {

    PomodoroRepository getPomodoroRepository();
    PomodoroMapper getPomodoroMapper();
    PomodoroPeriod pomodoroPeriod();

    List<PomodoroDto> provide(String tag);

    default List<PomodoroDto> provide(ZonedDateTime startRange, ZonedDateTime endRange, String tagName) { //TODO: move this method to another class to not expose it as it used only by providers
        if (tagName == null) {
            return getPomodoroRepository().findByStartTimeAfterAndEndTimeBeforeOrderByStartTime(startRange, endRange).stream()
                    .map(getPomodoroMapper()::mapToDto)
                    .sorted(Comparator.comparing(PomodoroDto::getStartTime))
                    .collect(Collectors.toList());
        }

        return getPomodoroRepository().findByStartTimeAfterAndEndTimeBeforeOrderByStartTime(startRange, endRange).stream()
                .filter(pomodoro -> filterByTag(pomodoro, tagName))
                .map(getPomodoroMapper()::mapToDto)
                .sorted(Comparator.comparing(PomodoroDto::getStartTime))
                .collect(Collectors.toList());
    }

    private boolean filterByTag(Pomodoro pomodoro, String tagName) {
        PomodoroTag tag = pomodoro.getTag();

        if (tag == null) {
            return false;
        }

        return tagName.equals(tag.getName()) || pomodoroParentTagEqualToTag(tag.getParent(), tagName);
    }

    private boolean pomodoroParentTagEqualToTag(PomodoroTag parentTag, String tagName) {
        return parentTag != null && tagName.equals(parentTag.getName());
    }

}
