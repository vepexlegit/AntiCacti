package de.vepexlegit.anticacti;

import net.minecraft.launchwrapper.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;

public class ClassTransformer implements IClassTransformer {
    public static final String[] classesBeingTransformed = { "net.minecraft.block.BlockCactus" };

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        boolean isObfuscated = !name.equals(transformedName);
        int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
        return index != -1 ? transform(index, basicClass, isObfuscated) : basicClass;
    }

    private static byte[] transform(int index, byte[] basicClass, boolean isObfuscated) {
        System.out.println("Transforming: " + classesBeingTransformed[index]);
        try {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            cr.accept(cn, 0);
            switch (index) {
                case 0:
                    transformBlockCactus(cn, isObfuscated);
                    break;
            }
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return basicClass;
    }

    private static void transformBlockCactus(ClassNode blockCactusClass, boolean isObfuscated) {
        System.out.println("Transforming BlockCactus class...");
        final String ENTITY_COLLIDE = isObfuscated ? "a" : "onEntityCollidedWithBlock";
        final String ENTITY_COLLIDE_DESC = isObfuscated ? "(Ladm;Lcj;Lalz;Lpk;)V" : "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V";
        final String ATTACK_ENTITY_FROM = isObfuscated ? "a" : "attackEntityFrom";
        final String ATTACK_ENTITY_FROM_DESC = isObfuscated ? "(Low;F)Z" : "(Lnet/minecraft/util/DamageSource;F)Z";
        for (MethodNode method : blockCactusClass.methods) {
            System.out.println("Method found: " + method.name + " with desc: " + method.desc);
            if (method.name.equals(ENTITY_COLLIDE) && method.desc.equals(ENTITY_COLLIDE_DESC)) {
                System.out.println("Found method: " + ENTITY_COLLIDE);
                System.out.println("Found desc: " + ENTITY_COLLIDE_DESC);
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode)instruction).name.equals(ATTACK_ENTITY_FROM) && ((MethodInsnNode)instruction).desc.equals(ATTACK_ENTITY_FROM_DESC)) {
                        InsnList newInstructions = new InsnList();
                        newInstructions.add(new InsnNode(Opcodes.RETURN));
                        method.instructions.insert(instruction, newInstructions);
                        method.instructions.remove(instruction);
                        System.out.println("Replaced attackEntityFrom with RETURN.");
                        break;
                    }
                }
            }
        }
    }
}