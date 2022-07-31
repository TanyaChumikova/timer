package com.igorgorbunov3333.timer.service.tag.bunch;

import com.igorgorbunov3333.timer.model.entity.pomodoro.PomodoroTag;
import com.igorgorbunov3333.timer.model.entity.pomodoro.PomodoroTagBunch;
import com.igorgorbunov3333.timer.repository.PomodoroTagBunchRepository;
import com.igorgorbunov3333.timer.repository.PomodoroTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PomodoroTagBunchServiceTest {

    @InjectMocks
    private PomodoroTagBunchService testee;

    @Mock
    private PomodoroTagBunchRepository pomodoroTagBunchRepository;
    @Mock
    private PomodoroTagRepository pomodoroTagRepository;

    @Captor
    private ArgumentCaptor<PomodoroTagBunch> pomodoroTagBunchArgumentCaptor;

    @Test
    void saveBunch_WhenTagsEmpty_ThenDoNothing() {
        testee.saveBunch(Set.of());

        verifyNoInteractions(pomodoroTagBunchRepository, pomodoroTagRepository);
    }

    @Test
    void saveBunch_WhenTagsPresentInDatabaseAndNoBunchesWithExactTags_ThenSaveNewBunch() {
        Set<String> tagNames = Set.of("firstTag", "secondTag", "thirdTag");

        PomodoroTag tag1 = mock(PomodoroTag.class);
        PomodoroTag tag2 = mock(PomodoroTag.class);
        PomodoroTag tag3 = mock(PomodoroTag.class);

        when(pomodoroTagRepository.findByNameIn(tagNames)).thenReturn(List.of(tag1, tag2, tag3));

        PomodoroTagBunch bunch1 = mock(PomodoroTagBunch.class);
        when(bunch1.getPomodoroTags()).thenReturn(List.of(tag1));
        PomodoroTagBunch bunch2 = mock(PomodoroTagBunch.class);
        when(bunch2.getPomodoroTags()).thenReturn(List.of(tag2));
        when(pomodoroTagBunchRepository.findAll()).thenReturn(List.of(bunch1, bunch2));

        testee.saveBunch(tagNames);

        verify(pomodoroTagBunchRepository).save(pomodoroTagBunchArgumentCaptor.capture());

        PomodoroTagBunch actual = pomodoroTagBunchArgumentCaptor.getValue();
        assertThat(actual.getPomodoroTags()).containsExactly(tag1, tag2, tag3);
    }

    @Test
    void saveBunch_WhenBunchWithExactTagsPresentInDatabase_ThenDoNothing() {
        Set<String> tagNames = Set.of("firstTag", "secondTag", "thirdTag");

        PomodoroTag tag1 = mock(PomodoroTag.class);
        PomodoroTag tag2 = mock(PomodoroTag.class);
        PomodoroTag tag3 = mock(PomodoroTag.class);

        when(pomodoroTagRepository.findByNameIn(tagNames)).thenReturn(List.of(tag1, tag2, tag3));

        PomodoroTagBunch bunch1 = mock(PomodoroTagBunch.class);
        when(bunch1.getPomodoroTags()).thenReturn(List.of(tag1, tag2, tag3));
        PomodoroTagBunch bunch2 = mock(PomodoroTagBunch.class);
        when(pomodoroTagBunchRepository.findAll()).thenReturn(List.of(bunch1, bunch2));

        testee.saveBunch(tagNames);

        verify(pomodoroTagBunchRepository, never()).save(pomodoroTagBunchArgumentCaptor.capture());



    }

}
