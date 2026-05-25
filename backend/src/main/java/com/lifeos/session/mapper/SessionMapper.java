package com.lifeos.session.mapper;

import com.lifeos.metadata.enums.ContentType;
import com.lifeos.session.dto.CreateSessionRequest;
import com.lifeos.session.dto.SessionDto;
import com.lifeos.session.entity.ActivitySession;
import com.lifeos.session.enums.SessionState;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public SessionDto toDto(ActivitySession entity) {
        if (entity == null) {
            return null;
        }

        var builder = SessionDto.builder()
                .id(entity.getId())
                .packageName(entity.getPackageName())
                .rawTitle(entity.getRawTitle())
                .normalizedTitle(entity.getNormalizedTitle())
                .contentType(entity.getContentType())
                .sourcePlatform(entity.getSourcePlatform())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .durationSeconds(entity.getDurationSeconds())
                .sessionState(entity.getSessionState());

        if (entity.getMetadata() != null) {
            builder.metadataId(entity.getMetadata().getId())
                   .resolvedTitle(entity.getMetadata().getTitle())
                   .imageUrl(entity.getMetadata().getImageUrl());
        }

        return builder.build();
    }

    public ActivitySession toEntity(CreateSessionRequest request) {
        if (request == null) {
            return null;
        }

        ContentType type = null;
        if (request.getContentType() != null) {
            try {
                type = ContentType.valueOf(request.getContentType().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid content types
            }
        }

        SessionState state = SessionState.COMPLETED;
        if (request.getSessionState() != null) {
            try {
                state = SessionState.valueOf(request.getSessionState().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid session states
            }
        } else if (request.getEndedAt() == null) {
            state = SessionState.ACTIVE;
        }

        String platform = request.getSourcePlatform() != null ? request.getSourcePlatform() : "UNKNOWN";

        return ActivitySession.builder()
                .packageName(request.getPackageName())
                .rawTitle(request.getRawTitle())
                .contentType(type)
                .sourcePlatform(platform)
                .startedAt(request.getStartedAt())
                .endedAt(request.getEndedAt())
                .durationSeconds(request.getDurationSeconds())
                .sessionState(state)
                .build();
    }
}
