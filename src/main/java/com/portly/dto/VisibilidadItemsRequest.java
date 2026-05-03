package com.portly.dto;

import lombok.Data;

import java.util.Map;

@Data
public class VisibilidadItemsRequest {
    private Boolean showProjects;
    private Map<String, Boolean> techSkillItems;
    private Map<String, Boolean> softSkillItems;
    private Map<String, Boolean> experienceItems;
    private Map<String, Boolean> educationItems;
    private Map<String, Boolean> projectItems;
}
