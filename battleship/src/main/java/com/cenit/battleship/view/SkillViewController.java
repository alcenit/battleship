/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.view;

import com.cenit.battleship.model.Skill;
import com.cenit.battleship.model.SkillSystem;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 *
 * @author Usuario
 */
public class SkillViewController implements Initializable {
    
    @FXML 
    private VBox skillConteiner;
    @FXML 
    private Label lblPoints;
    @FXML 
    private Button btnClose;
    
    private SkillSystem skillsSystem;
    private SkillClickListener clickListener;

    public interface SkillClickListener {
        void onHabilidadClick(Skill skill);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls();
    }

    public void setSkillSystem(SkillSystem system) {
        this.skillsSystem = system;
        updateView();
    }

    public void setSkillClickListener(SkillClickListener listener) {
        this.clickListener = listener;
    }

    private void setupControls() {
        btnClose.setOnAction(e -> closeDialogue());
    }

    private void updateView() {
        skillConteiner.getChildren().clear();
        lblPoints.setText("Puntos: " + skillsSystem.getSkillPoints());

        for (Skill  skill : skillsSystem.getAvailableSkills().keySet()) {
            Button btnSkill = createSkillButton(skill);
            skillConteiner.getChildren().add(btnSkill);
        }
    }

    private Button createSkillButton(Skill skill) {
        Button button = new Button();
        button.setPrefSize(200, 80);
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        String texto = String.format("%s\n%d puntos | %d usos\n%s",
            skill.getName(),
            skill.getCost(),
            skillsSystem.getRemainingUses().get(skill),
            skill.getDescription()
        );
        
        button.setText(texto);
        button.setWrapText(true);
        
        // Deshabilitar si no se puede usar
        if (!skillsSystem.canUseSkill(skill)) {
            button.setDisable(true);
            button.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");
        }

        button.setOnAction(e -> {
            if (clickListener != null) {
                clickListener.onHabilidadClick(skill);
            }
        });

        return button;
    }

    private void closeDialogue() {
        btnClose.getScene().getWindow().hide();
    }
}
