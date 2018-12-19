package com.miolean.arena.entities;

import com.miolean.arena.framework.Option;

import java.awt.*;
import java.io.InputStream;

import static com.miolean.arena.entities.Arena.ARENA_SIZE;
import static com.miolean.arena.framework.UByte.ub;


@SuppressWarnings("unused")
public class DefaultGeneticRobot extends GeneticRobot {


    private int viewDistance = 10;

    private boolean equalFlag = false;
    private boolean greaterFlag = false;

    static final int TYPE_TANK = 0x0;
    static final int TYPE_COG = 0x4;
    static final int TYPE_BULLET = 0x8;
    static final int TYPE_WALL = 0x12;


    //This should really be an Option
    static final int MAX_STACK_SIZE = 16;


    public DefaultGeneticRobot(GeneticRobot parent, Arena arena) {
        super(parent, arena);
    }
    public DefaultGeneticRobot(InputStream file, Arena arena) {
        super(file, arena);
    }

    private int typeOf(Entity entity) {
        //It's sort of fun to decomment these as I add new stuff.
        if (entity instanceof Robot) return TYPE_TANK;
        if (entity instanceof Cog) return TYPE_COG;
        if (entity instanceof Bullet) return TYPE_BULLET;
        //if (entity instanceof Wall) return TYPE_WALL;
        return 0;
    }

    /*-----------------------------------------------------------------
     * Reflected methods (genes) are below.
     * Beware. Not intended for human consumption.
     * All arguments are intended to be within the range [0, 255].
     */

    public void _UNDEF() {}
    public void _UNDEF(int... args) {}
    public void _NO () {} //"Nothing" that is registered as gene #0 (which is the default value)
    public void _SNO() {} //"Nothing" that is registered as gene #1 (not the default value and therefore an actual gene)
    public void _PRINT(int arg0, int arg1, int arg2) {
        //System.out.printf("%s says: %s, %s, %s.\n", getName(), WMEM[arg0].val(), WMEM[arg1].val(), WMEM[arg2].val());
    }

    //TODO Implement test commands after we can disable selected genes
    public void _SCALC() {}
    public void _TCALC() {}
    public void _HURT() {}

    public void _GOTO (int reg) {index = WMEM[reg].val() - 2;}
    public void _GOE  (int reg) {if(equalFlag) index = WMEM[reg].val() - 2;}
    public void _GOG  (int reg) {if(greaterFlag) index = WMEM[reg].val() - 2;}
    public void _IGOTO(int immed) {index = immed - 2;}
    public void _IGOE (int immed) {if(equalFlag) index = immed - 2;}
    public void _IGOG (int immed) {if(greaterFlag) index = immed - 2;}

    public void _COMP (int reg1, int reg2) {
        equalFlag = (WMEM[reg1].val() == WMEM[reg2].val());
        greaterFlag = (WMEM[reg1].val() > WMEM[reg2].val());
    }
    public void _ICOMP (int reg, int immed) {
        equalFlag = (WMEM[reg].val() == immed);
        greaterFlag = (WMEM[reg].val() > immed);
    }

    public void _RUN (int reg) {
        if(stack.size() < MAX_STACK_SIZE) {
            stack.push(new Point(loaded, index));
            loaded = WMEM[reg].val();
            index = 0;
        }
    }
    public void _RUNE  (int reg) {
        if(stack.size() < MAX_STACK_SIZE && equalFlag) {
            stack.push(new Point(loaded, index));
            loaded = WMEM[reg].val();
            index = 0;
        }
    }
    public void _RUNG  (int reg) {
        if(stack.size() < MAX_STACK_SIZE && greaterFlag) {
            stack.push(new Point(loaded, index));
            loaded = WMEM[reg].val();
            index = 0;
        }
    }
    public void _IRUN(int immed) {
        if(stack.size() < MAX_STACK_SIZE) {
            stack.push(new Point(loaded, index));
            loaded = immed;
            index = 0;
        }
    }
    public void _IRUNE (int immed) {
        if(stack.size() < MAX_STACK_SIZE && equalFlag) {
            stack.push(new Point(loaded, index));
            loaded = immed;
            index = 0;
        }
    }
    public void _IRUNG (int immed) {
        if(stack.size() < MAX_STACK_SIZE && greaterFlag) {
            stack.push(new Point(loaded, index));
            loaded = immed;
            index = 0;
        }
    }

    //Store values in places from existing values in registries
    public void _MOV (int targReg, int sourceReg) {
        WMEM[targReg] = WMEM[sourceReg];
    }
    public void _SSTO (int targSMemReg, int targAddrReg, int sourceReg) {
        if(SMEM[WMEM[targSMemReg].val()] != null) {
            SMEM[WMEM[targSMemReg].val()][WMEM[targAddrReg].val()] = WMEM[sourceReg];
        }
    }
    public void _PSTO (int targPMemReg, int targAddrReg, int sourceReg) {
        if(PMEM[WMEM[targPMemReg].val()] != null) {
            PMEM[WMEM[targPMemReg].val()][WMEM[targAddrReg].val()] = WMEM[sourceReg];
        }
    }
    public void _USTO (int targUMemReg, int targAddrReg, int sourceReg) {
        if(UMEM[WMEM[targUMemReg].val()] != null) {
            UMEM[WMEM[targUMemReg].val()][WMEM[targAddrReg].val()] = WMEM[sourceReg];
        }
    }

    //Put values in the registry from existing values in memories
    public void _SGET (int targReg, int sourceSMemReg, int sourceAddrReg) {
        if(SMEM[WMEM[sourceSMemReg].val()] != null) {
            WMEM[targReg] = SMEM[WMEM[sourceSMemReg].val()][WMEM[sourceAddrReg].val()];
        }
    }
    public void _PGET (int targReg, int sourcePMemReg, int sourceAddrReg) {
        if(PMEM[WMEM[sourcePMemReg].val()] != null) {
            WMEM[targReg] = PMEM[WMEM[sourcePMemReg].val()][WMEM[sourceAddrReg].val()];
        }
    }
    public void _UGET (int targReg, int sourceUMemReg, int sourceAddrReg) {
        if(UMEM[WMEM[sourceUMemReg].val()] != null) {
            WMEM[targReg] = UMEM[WMEM[sourceUMemReg].val()][WMEM[sourceAddrReg].val()];
        }
    }

    //Put immediate values in immediate locations
    public void _IMOV (int targReg, int immed) {
        WMEM[targReg] = ub(immed);
    }
    public void _ISSTO(int targSMem, int targAddr, int immed) {
        if(SMEM[targSMem] != null) {
            SMEM[targSMem][targAddr] = ub(immed);
        }
    }
    public void _IPSTO(int targSMem, int targAddr, int immed) {
        if(PMEM[targSMem] != null) {
            PMEM[targSMem][targAddr] = ub(immed);
        }
    }
    public void _IUSTO(int targSMem, int targAddr, int immed) {
        if(UMEM[targSMem] != null) {
            UMEM[targSMem][targAddr] = ub(immed);
        }
    }

    //Clear an area of memories
    public void _WCLR (int start, int end) {
        while(start < end && start < 256) {
            WMEM[start] = ub(0);
            start++;
        }
    }
    public void _SCLR (int targSMemreg, int startReg, int endReg) {
        if(SMEM[WMEM[targSMemreg].val()] != null) {
            int counter = WMEM[startReg].val();
            while(counter < WMEM[endReg].val() && counter < 256) {
                SMEM[WMEM[targSMemreg].val()][counter] = ub(0);
                counter++;
            }
        }
    }
    public void _PCLR (int targSMemreg, int startReg, int endReg) {
        if(PMEM[WMEM[targSMemreg].val()] != null) {
            int counter = WMEM[startReg].val();
            while(counter < WMEM[endReg].val() && counter < 256) {
                PMEM[WMEM[targSMemreg].val()][counter] = ub(0);
                counter++;
            }
        }
    }
    public void _UCLR (int targSMemreg, int startReg, int endReg) {
        if(UMEM[WMEM[targSMemreg].val()] != null) {
            int counter = WMEM[startReg].val();
            while(counter < WMEM[endReg].val() && counter < 256) {
                UMEM[WMEM[targSMemreg].val()][counter] = ub(0);
                counter++;
            }
        }
    }

    //Get information about where the Robot is and how fast it's going.
    public void _POSX (int targReg) {WMEM[targReg] = ub((int) (getX()/ARENA_SIZE*255));}
    public void _VELX (int targReg) {WMEM[targReg] = ub((int) getVelX());}
    public void _ACCX (int targReg) {WMEM[targReg] = ub((int) getAccX());}
    public void _POSY (int targReg) {WMEM[targReg] = ub((int) (getY()/ARENA_SIZE*255));}
    public void _VELY (int targReg) {WMEM[targReg] = ub((int) getVelY());}
    public void _ACCY (int targReg) {WMEM[targReg] = ub((int) getAccY());}
    public void _POSR (int targReg) {WMEM[targReg] = ub((int) (getR()/(2*Math.PI)*255));}
    public void _VELR (int targReg) {WMEM[targReg] = ub((int) getVelR());}
    public void _ACCR (int targReg) {WMEM[targReg] = ub((int) getAccR());}

    //TODO Find other nearby entities
    public void _VIEW (int viewReg) {viewDistance = WMEM[viewReg].val();}
    public void _NEAR (int targReg) {}
    public void _CNEAR (int targReg) {}
    public void _RNEAR (int targReg) {}
    public void _BNEAR (int targReg) {}
    public void _WNEAR (int targReg) {}
    public void _NST (int targReg) {}
    public void _CNST (int targReg) {}
    public void _RNST (int targReg) {}
    public void _BNST (int targReg) {}
    public void _WNST (int targReg) {}

    //General actions
    public void _HEAL () {repair();}
    public void _FORWD(int forceReg) {forward(WMEM[forceReg].val());}
    public void _REVRS(int forceReg) {forward(-WMEM[forceReg].val());}
    public void _FIRE () {fire();}
    public void _TURNL(int forceReg) {rotate(WMEM[forceReg].val());}
    public void _TURNR(int forceReg) {rotate(-WMEM[forceReg].val());}

    public void _WALL (int lengthReg, int widthReg) {} //TODO Implement _WALL() [when Walls exist]
    public void _SPIT (int valueReg) {} //TODO Implement _SPIT() [when Cogs exist]
    public void _HUE (int sourceReg) {setHue(WMEM[sourceReg].val());}
    public void _FACE(int uuidReg) {} //TODO Face towards an entity

    //Math
    public void _ADD  (int arg0, int arg1) {WMEM[arg0] = ub(WMEM[arg0].val() + WMEM[arg1].val());}
    public void _SUB  (int arg0, int arg1) {WMEM[arg0] = ub(WMEM[arg0].val() + WMEM[arg1].val());}
    public void _PROD (int arg0, int arg1) {WMEM[arg0] = ub(WMEM[arg0].val() + WMEM[arg1].val());}
    public void _QUOT (int arg0, int arg1) {if(WMEM[arg1].val() != 0) WMEM[arg0] = ub(WMEM[arg0].val() + WMEM[arg1].val());}
    public void _INCR (int arg0) {WMEM[arg0] = ub(WMEM[arg0].val() + 1);}

    //Identity of other Robots
    public void _OTYPE(int targReg, int uuidReg) {if(uuidReg < 255) WMEM[targReg] = ub(typeOf(getArena().fromUUID(WMEM[uuidReg].val(),WMEM[uuidReg+1].val())));}
    public void _OHP  (int targReg, int uuidReg) {if(uuidReg < 255) WMEM[targReg] = ub((int) (getArena().fromUUID(WMEM[uuidReg].val(),WMEM[uuidReg+1].val()).getHealth()));}
    public void _OCOG (int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),WMEM[uuidReg+1].val()) instanceof Robot) WMEM[targReg] = ub((int)((Robot) getArena().fromUUID(WMEM[uuidReg].val(),WMEM[uuidReg+1].val())).getCogs());}
    public void _OHUE (int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),WMEM[uuidReg+1].val()) instanceof Robot) WMEM[targReg] = ub((int)((Robot) getArena().fromUUID(WMEM[uuidReg].val(),WMEM[uuidReg+1].val())).getHue());}
    public void _OFIT (int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),WMEM[uuidReg+1].val()) instanceof Robot) WMEM[targReg] = ub((int)((GeneticRobot) getArena().fromUUID(WMEM[uuidReg].val(),WMEM[uuidReg+1].val())).getFitness());}

    public void _UUID(int targReg) {if(targReg < 255) { WMEM[targReg] = ub(getUUID()>>8); WMEM[targReg] = ub(getUUID());}}
    public void _HP   (int targReg) {WMEM[targReg] = ub((int) getHealth());}
    public void _COG  (int targReg) {WMEM[targReg] = ub((int) getCogs());}
    public void _PNT  (int targReg) {WMEM[targReg] = ub((int) getFitness());}

    public void _UPG  (int statReg, int amountReg) {upgrade(WMEM[statReg], WMEM[amountReg].val());}
    public void _STAT (int targReg, int statReg) {WMEM[targReg] = stats[Math.abs(statReg>>5)];}
    public void _KWGT (int targReg, int kAddrReg) {if(KMEM[WMEM[kAddrReg].val()] != null) WMEM[targReg] = ub(KMEM[WMEM[kAddrReg].val()].getWeight());}
    public void _COST (int targReg, int kAddrReg) {if(KMEM[WMEM[kAddrReg].val()] != null) WMEM[targReg] = ub((int)(KMEM[WMEM[kAddrReg].val()].getCost()*4));}

    public void _OPOSX(int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg+1].val()) != null) WMEM[targReg] = ub((int) getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg].val()+1).getX());}
    public void _OVELX(int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg+1].val()) != null) WMEM[targReg] = ub((int) getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg].val()+1).getVelX());}
    public void _OACCX(int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg+1].val()) != null) WMEM[targReg] = ub((int) getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg].val()+1).getAccX());}
    public void _OPOSY(int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg+1].val()) != null) WMEM[targReg] = ub((int) getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg].val()+1).getY());}
    public void _OVELY(int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg+1].val()) != null) WMEM[targReg] = ub((int) getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg].val()+1).getVelY());}
    public void _OACCY(int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg+1].val()) != null) WMEM[targReg] = ub((int) getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg].val()+1).getAccY());}
    public void _OPOSR(int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg+1].val()) != null) WMEM[targReg] = ub((int) getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg].val()+1).getR());}
    public void _OVELR(int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg+1].val()) != null) WMEM[targReg] = ub((int) getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg].val()+1).getVelR());}
    public void _OACCR(int targReg, int uuidReg) {if(uuidReg < 255 && getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg+1].val()) != null) WMEM[targReg] = ub((int) getArena().fromUUID(WMEM[uuidReg].val(),  WMEM[uuidReg].val()+1).getAccR());}

    //TODO Better manage multiple memories
    public void _DEFS (int newMemReg) {if(SMEM[WMEM[newMemReg].val()] == null) createMemory(SMEM, WMEM[newMemReg].val());}
    public void _DELS (int memReg) {if(SMEM[WMEM[memReg].val()] != null && WMEM[memReg].val() != 0) SMEM[WMEM[memReg].val()] = null;}
    public void _DEFP (int newMemReg) {if(PMEM[WMEM[newMemReg].val()] == null) createMemory(PMEM, WMEM[newMemReg].val());}
    public void _DELP (int memReg) {if(SMEM[WMEM[memReg].val()] != null && WMEM[memReg].val() != 0 && !(WMEM[memReg].val() == loaded && CURRENT == PMEM)) PMEM[WMEM[memReg].val()] = null;}
    public void _DEFU (int newMemReg) {if(UMEM[WMEM[newMemReg].val()] == null) createMemory(UMEM, WMEM[newMemReg].val());}
    public void _DELU (int memReg) {if(SMEM[WMEM[memReg].val()] != null && WMEM[memReg].val() != 0 && !(WMEM[memReg].val() == loaded && CURRENT == UMEM)) UMEM[WMEM[memReg].val()] = null;}
    public void _LOADED(int targReg) {WMEM[targReg] = ub(loaded);}

    //TODO Get rid of this awful, awful command
    @Deprecated
    public void _SWAP(int arg0, int arg1, int arg2) {
        if(CURRENT[arg0] != null) {
            loaded = arg0;
            index = arg1 - 4;
        }
    }

    //TODO Mass gene exchange and crossing commands
    public void _TARG() {}
    public void _SCOPY() {}
    public void _PCOPY() {}
    public void _UCOPY() {}

    public void _REP  () {reproduce();}
    public void _TWK  (int geneToTweak, int sourceReg) {if(KMEM[geneToTweak] != null) KMEM[geneToTweak].setWeight(WMEM[sourceReg].val());}
    public void _KRAND(int targReg) {WMEM[targReg] = randomGene();}
    public void _URAND(int targReg, int sourceUMem) {if(UMEM[sourceUMem] != null) WMEM[targReg] = randomAddress(UMEM[sourceUMem]);}
    public void _PRAND(int targReg, int sourceUMem) {if(PMEM[sourceUMem] != null) WMEM[targReg] = randomAddress(PMEM[sourceUMem]);}
    public void _SRAND(int targReg, int sourceUMem) {if(SMEM[sourceUMem] != null) WMEM[targReg] = randomAddress(SMEM[sourceUMem]);}
    public void _WRAND(int targReg) {WMEM[targReg] = randomAddress(WMEM);}
    public void _IRAND(int targReg) {WMEM[targReg] = ub((int) (Option.random.nextFloat() * 255));}

}
