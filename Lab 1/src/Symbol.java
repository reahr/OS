import java.util.ArrayList;

/**
 * Name: Reah Rajmangal
 * The Symbol class creates a symbol with definition and other properties that are altered if it is used or duplicated
 */

class Symbol {
    String name;
    Integer address;
    int moduleNumber;
    int duplicate;
    int used;
    static ArrayList<Symbol> symbols = new ArrayList<>();

    /**
     * @param name         String representation of the symbol
     * @param address      Absolute address where Symbol is defined
     * @param moduleNumber Module where Symbol is defined
     */
    public Symbol(String name, Integer address, int moduleNumber) {
        this.name = name;
        this.address = address;
        this.duplicate = 0;
        this.used = -1;
        this.moduleNumber = moduleNumber;
    }

    /**
     * Sets Symbol duplicate to 1 if it is used more than once
     */
    void setDuplicate() {
        this.duplicate = 1;
    }

    /**
     * Sets Symbol used to the latest module this Symbol was used in
     * This is used to check if symbol in a Use List is actually used in that module
     *
     * @param moduleNumber latest module it was used in
     */
    void setUsed(int moduleNumber) {
        this.used = moduleNumber;
    }

    /**
     * @return String representation of this Symbol
     */
    @Override
    public String toString() {
        if (this.duplicate == 1) return (name + "=" + address +
                " Error: This variable is multiply defined; first value used.\n");
        return (name + "=" + address + "\n");
    }

    /**
     * Checks if this Symbol is equal to another Symbol based off of its name
     *
     * @param o Object checked as Symbol, used to find a Symbol given the name of it
     * @return true if Symbol is equal to this Symbol
     */
    @Override
    public boolean equals(Object o) {
        Symbol x = (Symbol) o;
        if (x.name.equals(this.name)) return true;
        return false;
    }
}
