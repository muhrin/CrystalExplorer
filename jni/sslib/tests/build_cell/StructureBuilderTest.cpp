/*
 * StructureBuilderTest.cpp
 *
 *  Created on: Oct 3, 2012
 *      Author: Martin Uhrin
 */

// INCLUDES //////////////////////////////////
#include "sslibtest.h"

#include <boost/foreach.hpp>

#include <build_cell/BuildCellFwd.h>
#include <build_cell/AtomsDescription.h>
#include <build_cell/AtomsGenerator.h>
#include <build_cell/IUnitCellGenerator.h>
#include <build_cell/GenerationOutcome.h>
#include <build_cell/StructureBuilder.h>
#include <common/AtomSpeciesDatabase.h>
#include <common/AtomSpeciesId.h>
#include <common/Structure.h>
#include <common/Types.h>

namespace ssbc = ::sstbx::build_cell;
namespace ssc  = ::sstbx::common;


BOOST_AUTO_TEST_CASE(StructureBuilderClusterTest)
{
  typedef ::std::pair<ssc::AtomSpeciesId::Value, unsigned int> SpeciesCount;
  //// Settings ////////////////
  const unsigned int TIMES_TO_GENERATE = 10;
  
  ::std::vector<SpeciesCount> toGenerate;
  toGenerate.push_back(SpeciesCount("Na", 2));
  toGenerate.push_back(SpeciesCount("Cl", 5));
  toGenerate.push_back(SpeciesCount("H", 10));

  unsigned int totalAtoms = 0;
  BOOST_FOREACH(const SpeciesCount & speciesCount, toGenerate)
    totalAtoms += speciesCount.second;
  
  ssbc::StructureBuilder builder;
  {
    ssbc::AtomsGeneratorConstructionInfo constructionInfo;
    BOOST_FOREACH(const SpeciesCount & speciesCount, toGenerate)
      constructionInfo.atoms.push_back(ssbc::AtomsDescription(speciesCount.first, speciesCount.second));
    
    builder.addGenerator(::sstbx::makeUniquePtr(new ssbc::AtomsGenerator(constructionInfo)));
  }

  ssc::AtomSpeciesDatabase speciesDb;
  ssc::StructurePtr structure;
  ssbc::GenerationOutcome outcome;
  for(unsigned int i = 0; i < TIMES_TO_GENERATE; ++i)
  {
    outcome = builder.generateStructure(structure, speciesDb);
    BOOST_CHECK(outcome.success());
    BOOST_REQUIRE(structure->getNumAtoms() == totalAtoms);
  }


}
