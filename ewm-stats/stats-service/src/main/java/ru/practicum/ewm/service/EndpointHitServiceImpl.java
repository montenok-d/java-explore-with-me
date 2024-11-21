package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.mapper.EndpointHitMapper;
import ru.practicum.ewm.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitRepository endpointHitRepository;

    public void save(EndpointHitDto endpointHitDto) {
        endpointHitRepository.save(EndpointHitMapper.toEndpointHit(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris,
                                       boolean unique) {
        if (unique && !uris.isEmpty()) {
            return endpointHitRepository.findAllHitsWithUniqueIpWithUris(uris, start, end);
        }
        if (unique) {
            return endpointHitRepository.findAllHitsWithUniqueIpWithoutUris(start, end);
        }
        if (!uris.isEmpty()) {
            return endpointHitRepository.findAllHitsWithUris(uris, start, end);
        }
        return endpointHitRepository.findAllHitsWithoutUris(start, end);
    }
}
