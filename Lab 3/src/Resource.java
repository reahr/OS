/**
 * Reah Rajmangal, Lab 3
 * This class represents a resource which has an ID (num > 0) and amount (which can be changed at any given time)
*/

class Resource{
    int id;
    int amount;

    Resource (int id, int amount){
        this.id=id;
        this.amount=amount; //set to initial amount
    }

    /**
     * String representation of a resource
     * @return String that represents a Resource object
     */
    @Override
    public String toString(){
        return String.format("ID: %d, Amount: %d", id, amount);
    }


}
