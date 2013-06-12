/*
 * AtomExtruderTest.cpp
 *
 *  Created on: Aug 18, 2011
 *      Author: Martin Uhrin
 */

// INCLUDES //////////////////////////////////
#include "sslibtest.h"

#include <boost/filesystem.hpp>
#include <boost/foreach.hpp>

#include <common/AtomSpeciesId.h>
#include <common/Structure.h>
#include <common/Types.h>
#include <common/UnitCell.h>
#include <io/CellReaderWriter.h>
#include <io/ResourceLocator.h>
#include <io/ResReaderWriter.h>
#include <io/SslibReaderWriter.h>
#include <io/StructureReadWriteManager.h>
#include <utility/StableComparison.h>

namespace fs = ::boost::filesystem;
namespace ssio = ::sstbx::io;
namespace ssc = ::sstbx::common;
namespace compare = ::sstbx::utility::stable;

void checkSimilar(const ssc::Structure & str1, const ssc::Structure & str2);

BOOST_AUTO_TEST_CASE(ReaderWriterTest)
{
  // SETTINGS ///////
  const size_t NUM_READER_WRITERS = 3;
  const size_t NUM_ATOMS = 32;
  const ::std::string SAVE_PATH = "rwTest.out";

  // Set up all the readers/writers we want to test
  ssio::StructureReadWriteManager rwMan;
  rwMan.insert(::sstbx::makeUniquePtr(new ssio::ResReaderWriter()));
  rwMan.insert(::sstbx::makeUniquePtr(new ssio::SslibReaderWriter()));
  rwMan.insert(::sstbx::makeUniquePtr(new ssio::CellReaderWriter()));
  BOOST_REQUIRE(rwMan.numReaders() == NUM_READER_WRITERS);
  BOOST_REQUIRE(rwMan.numWriters() == NUM_READER_WRITERS);

  // Set up the structure to write
  ssc::Structure structure;
  ::arma::vec3 pos;
  ssc::AtomSpeciesId::Value species = "Na";
  for(size_t i = 0; i < NUM_ATOMS; ++i)
  {
    pos.randu();
    structure.newAtom(species).setPosition(pos);
  }
  
  ssio::ResourceLocator saveTo;
  saveTo.setId("rwTest");
  ssc::StructurePtr loadedStructure;
  for(ssio::StructureReadWriteManager::WritersIterator it = rwMan.beginWriters(),
    end = rwMan.endWriters(); it != end; ++it)
  {
    saveTo.setPath(fs::path(SAVE_PATH + "." + it->first));
    it->second->writeStructure(structure, saveTo);

    loadedStructure = rwMan.readStructure(saveTo);
    BOOST_CHECK(loadedStructure.get());

    if(loadedStructure.get())
      checkSimilar(structure, *loadedStructure);
  }

}

void checkSimilar(const ssc::Structure & str1, const ssc::Structure & str2)
{
  BOOST_REQUIRE(str1.getNumAtoms() == str2.getNumAtoms());

  const size_t numAtoms = str1.getNumAtoms();

  ::std::vector<ssc::AtomSpeciesId::Value> species1, species2;
  str1.getAtomSpecies(species1);
  str2.getAtomSpecies(species2);
  BOOST_REQUIRE(species1.size() == species2.size());

  // Put all the species in structure 1 into a set and one by one insert those from
  // structure 2 and make sure no new ones are inserted
  ::std::set<ssc::AtomSpeciesId::Value> speciesSet(species1.begin(), species1.end());
  BOOST_FOREACH(const ssc::AtomSpeciesId::Value & spec, species2)
  {
    BOOST_REQUIRE(speciesSet.insert(spec).second == false);
    // Check the numbers of species match
    BOOST_REQUIRE(str1.getNumAtomsOfSpecies(spec) == str2.getNumAtomsOfSpecies(spec));
  }


  // Check the unit cell (if any)
  //const ssc::UnitCell * const uc1 = str1.getUnitCell();
  //const ssc::UnitCell * const uc2 = str2.getUnitCell();

  //BOOST_REQUIRE(!((uc1 == NULL) ^ (uc2 == NULL))); // Either have to be both NULL or both !NULL
  //if(uc1 && uc2)
  //{
  //  BOOST_REQUIRE(compare::eq(uc1->getVolume(), uc2->getVolume()));

  //  // TODO: More unit cell checks

  //}
}
