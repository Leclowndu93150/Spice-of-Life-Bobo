package com.leclowndu93150.spiceoflifebobo.data;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class FoodAttributeModifier {
    private final Attribute attribute;
    private final double amount;
    private final AttributeModifier.Operation operation;

    public FoodAttributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation) {
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public double getAmount() {
        return amount;
    }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }

    public AttributeModifier createModifier(UUID foodId) {
        UUID modifierId = new UUID(
                foodId.getMostSignificantBits(),
                foodId.getLeastSignificantBits() ^ attribute.getDescriptionId().hashCode()
        );

        return new AttributeModifier(
                modifierId,
                "Food effect: " + attribute.getDescriptionId(),
                amount,
                operation
        );
    }
}
