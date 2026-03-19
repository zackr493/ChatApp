from .dog import Dog
from .animal import Animal
from .animal import AnimalLike

def describe(animal: AnimalLike):
    print(animal.num_leg)
    print(animal.animal_type)

class Rock :
    def __init__(self):
        self.weight = 10

if __name__ == "__main__" :


    # INHERITANCE / ABSTRACTION
    # animal = Animal(0, "reptile")
    # you cant instantiate animal directly because it is an abstract class
    dog = Dog(4, "mammal")

    print("\n")

    print("Dog: " + str(dog))

    dog.sound()


    # PROTOCOLS
    # protocol provide a type structure, "if it looks like it , it is"
    # this works because dog has the correct parameters defined
    describe(dog)

    # this doesnt work because it doesnt have animal_type and num_legs
    rock = Rock()
    # describe(rock)


    pass

